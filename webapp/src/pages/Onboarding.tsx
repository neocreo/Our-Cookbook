// Onboarding — the simplified first-run flow. Replaces the old device
// registration screen entirely. Two first-class choices:
//   - "Logga in med Google Drive" (sync enabled)
//   - "Fortsätt offline" (local-only, can enable Drive later in Settings)
// A device id is generated silently in the background for sync metadata; the
// user never sees or interacts with it.

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BookOpen, Cloud, HardDrive } from 'lucide-react'
import { Button } from '../components/Button'
import { useAppStore, type SyncMode } from '../stores/appStore'
import { useT } from '../i18n'
import { getDeviceId } from '../lib/device'

export function Onboarding() {
  const navigate = useNavigate()
  const t = useT()
  const completeOnboarding = useAppStore((s) => s.completeOnboarding)
  const setUserName = useAppStore((s) => s.setUserName)
  const [name, setName] = useState('')

  function choose(mode: SyncMode) {
    getDeviceId() // silent — generated and persisted, never shown
    setUserName(name.trim() || t('onboarding.youDefault'))
    completeOnboarding(mode)
    // Drive choice continues on the sync page, where sign-in happens.
    navigate(mode === 'drive' ? '/sync' : '/', { replace: true })
  }

  return (
    <div className="onboarding">
      <div className="onboarding-mark" aria-hidden>
        <BookOpen strokeWidth={2.75} />
      </div>
      <div>
        <h1>Our Open Cookbook</h1>
        <p className="text-muted">{t('onboarding.tagline')}</p>
      </div>
      <div className="field">
        <label htmlFor="onboarding-name">{t('onboarding.nameLabel')}</label>
        <input
          id="onboarding-name"
          className="input"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder={t('onboarding.namePlaceholder')}
          autoFocus
        />
      </div>
      <div className="onboarding-actions">
        <Button variant="primary" block onClick={() => choose('drive')}>
          <Cloud strokeWidth={2.75} />
          {t('onboarding.signInDrive')}
        </Button>
        <Button variant="secondary" block onClick={() => choose('offline')}>
          <HardDrive strokeWidth={2.75} />
          {t('onboarding.continueOffline')}
        </Button>
      </div>
      <p className="text-muted text-xs">{t('onboarding.note')}</p>
    </div>
  )
}
