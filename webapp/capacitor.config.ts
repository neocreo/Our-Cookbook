import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.ourcookbook',
  appName: 'Our Open Cookbook',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    // iOS uses a custom scheme (capacitor://) by default, but Google OAuth
    // only accepts http/https origins. Force https://localhost so both
    // platforms share a single Google-authorized origin.
    iosScheme: 'https',
    hostname: 'localhost',
    // Keep Google's OAuth pages inside the WebView: the native sign-in flow
    // navigates to accounts.google.com and back with the token in the URL
    // fragment. Without this, the flow would open in the system browser and
    // the redirect back to https://localhost would never reach the app.
    allowNavigation: ['accounts.google.com'],
  },
}

export default config
