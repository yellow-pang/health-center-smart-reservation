FROM jenkins/jenkins:lts-jdk17

USER root

ARG NODE_MAJOR=20

RUN apt-get update \
    && apt-get install -y ca-certificates curl gnupg maven \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg \
      | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
    && chmod a+r /etc/apt/keyrings/docker.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
      > /etc/apt/sources.list.d/docker.list \
    && curl -fsSL https://deb.nodesource.com/setup_${NODE_MAJOR}.x | bash - \
    && apt-get update \
    && apt-get install -y docker-ce-cli docker-compose-plugin nodejs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
