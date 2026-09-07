import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.ourcookbook',
  appName: 'Our Cookbook',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    // iOS uses a custom scheme (capacitor://) by default, but Google OAuth
    // only accepts http/https origins. Force https://localhost so both
    // platforms share a single Google-authorized origin.
    iosScheme: 'https',
    hostname: 'localhost',
  },
}

export default config
