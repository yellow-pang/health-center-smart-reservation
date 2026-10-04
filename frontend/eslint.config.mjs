import { defineConfig, globalIgnores } from 'eslint/config'
import nextVitals from 'eslint-config-next/core-web-vitals'

export default defineConfig([
  ...nextVitals,
  {
    // Existing pages initialize client storage and data requests in effects.
    // Keep this React Compiler migration advisory visible without rewriting
    // those flows as part of enabling CI. Hook correctness rules remain errors.
    rules: { 'react-hooks/set-state-in-effect': 'warn' },
  },
  globalIgnores(['.next/**', 'out/**', 'build/**', 'next-env.d.ts']),
])
