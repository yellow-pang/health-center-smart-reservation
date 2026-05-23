import http from 'k6/http';
import { check, fail, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const STAFF_EMAIL = __ENV.STAFF_EMAIL || 'staff@test.com';
const STAFF_PASSWORD = __ENV.STAFF_PASSWORD || 'password1234';
const SERVICE_TYPE_CODE = __ENV.SERVICE_TYPE_CODE || 'VACCINATION';
const SERVICE_TYPE_ID = __ENV.SERVICE_TYPE_ID ? Number(__ENV.SERVICE_TYPE_ID) : null;
const VUS = Number(__ENV.VUS || 5);
const STEADY_DURATION = __ENV.DURATION || '1m';
const RAMP_UP_DURATION = __ENV.RAMP_UP_DURATION || '20s';
const RAMP_DOWN_DURATION = __ENV.RAMP_DOWN_DURATION || '20s';
const QUEUE_ACTION_RATE = Number(__ENV.QUEUE_ACTION_RATE || 0.3);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 1);
const ERROR_RATE_THRESHOLD = __ENV.ERROR_RATE_THRESHOLD || '0.01';
const READ_P95_MS = __ENV.READ_P95_MS || '1000';
const WRITE_P95_MS = __ENV.WRITE_P95_MS || '1500';

export const options = {
  scenarios: {
    vaccination_queue_flow: {
      executor: 'ramping-vus',
      stages: [
        { duration: RAMP_UP_DURATION, target: VUS },
        { duration: STEADY_DURATION, target: VUS },
        { duration: RAMP_DOWN_DURATION, target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: [`rate<${ERROR_RATE_THRESHOLD}`],
    'http_req_duration{api:auth_login}': [`p(95)<${READ_P95_MS}`],
    'http_req_duration{api:service_types}': [`p(95)<${READ_P95_MS}`],
    'http_req_duration{api:walk_in}': [`p(95)<${WRITE_P95_MS}`],
    'http_req_duration{api:queue_list}': [`p(95)<${READ_P95_MS}`],
    'http_req_duration{api:queue_action}': [`p(95)<${WRITE_P95_MS}`],
  },
};

export function setup() {
  const loginResponse = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: STAFF_EMAIL,
      password: STAFF_PASSWORD,
    }),
    jsonParams({ api: 'auth_login' }),
  );

  const loginOk = check(loginResponse, {
    'staff login succeeds': (response) => response.status === 200 && response.json('success') === true,
    'access token exists': (response) => Boolean(response.json('data.accessToken')),
  });

  if (!loginOk) {
    fail(`Staff login failed. status=${loginResponse.status} body=${loginResponse.body}`);
  }

  const accessToken = loginResponse.json('data.accessToken');
  let serviceTypeId = SERVICE_TYPE_ID;

  if (!serviceTypeId) {
    const serviceResponse = http.get(
      `${BASE_URL}/api/service-types`,
      authParams(accessToken, { api: 'service_types' }),
    );

    const serviceOk = check(serviceResponse, {
      'service types load succeeds': (response) => response.status === 200 && response.json('success') === true,
      'vaccination service type exists': (response) => {
        const serviceTypes = response.json('data') || [];
        return serviceTypes.some((serviceType) => serviceType.code === SERVICE_TYPE_CODE);
      },
    });

    if (!serviceOk) {
      fail(`Service type lookup failed. status=${serviceResponse.status} body=${serviceResponse.body}`);
    }

    serviceTypeId = serviceResponse
      .json('data')
      .find((serviceType) => serviceType.code === SERVICE_TYPE_CODE).id;
  }

  return {
    accessToken,
    serviceTypeId,
  };
}

export default function (context) {
  const visitor = buildVisitor();

  const walkInResponse = http.post(
    `${BASE_URL}/api/visits/walk-in`,
    JSON.stringify({
      serviceTypeId: context.serviceTypeId,
      visitorName: visitor.name,
      visitorPhone: visitor.phone,
    }),
    authParams(context.accessToken, { api: 'walk_in' }),
  );

  const walkInOk = check(walkInResponse, {
    'walk-in creates queue ticket': (response) => response.status === 201 && response.json('success') === true,
    'walk-in status is waiting': (response) => response.json('data.status') === 'WAITING',
    'queue ticket id exists': (response) => Boolean(response.json('data.queueTicketId')),
  });

  if (!walkInOk) {
    sleep(THINK_TIME_SECONDS);
    return;
  }

  const queueTicketId = walkInResponse.json('data.queueTicketId');

  const queueListResponse = http.get(
    `${BASE_URL}/api/queues?serviceTypeId=${context.serviceTypeId}&status=WAITING`,
    authParams(context.accessToken, { api: 'queue_list' }),
  );

  check(queueListResponse, {
    'waiting queue list succeeds': (response) => response.status === 200 && response.json('success') === true,
  });

  if (Math.random() < QUEUE_ACTION_RATE) {
    progressQueueTicket(context.accessToken, queueTicketId);
  }

  sleep(THINK_TIME_SECONDS);
}

function progressQueueTicket(accessToken, queueTicketId) {
  const actions = [
    ['call', 'CALLED'],
    ['start', 'IN_PROGRESS'],
    ['complete', 'COMPLETED'],
  ];

  for (const [action, expectedStatus] of actions) {
    const response = http.post(
      `${BASE_URL}/api/queues/${queueTicketId}/${action}`,
      null,
      authParams(accessToken, { api: 'queue_action', queueAction: action }),
    );

    const ok = check(response, {
      [`queue ${action} succeeds`]: (result) => result.status === 200 && result.json('success') === true,
      [`queue ${action} status is ${expectedStatus}`]: (result) => result.json('data.status') === expectedStatus,
    });

    if (!ok) {
      return;
    }
  }
}

function buildVisitor() {
  const raw = String((__VU * 100000 + __ITER) % 100000000).padStart(8, '0');
  return {
    name: `K6Vaccine${__VU}_${__ITER}`,
    phone: `010-${raw.slice(0, 4)}-${raw.slice(4)}`,
  };
}

function jsonParams(tags = {}) {
  return {
    headers: {
      'Content-Type': 'application/json',
    },
    tags,
  };
}

function authParams(accessToken, tags = {}) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    tags,
  };
}
