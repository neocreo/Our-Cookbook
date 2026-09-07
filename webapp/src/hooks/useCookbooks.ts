// useCookbooks — subscribes to the cookbook repository for a live list.

import { useEffect, useState } from 'react'
import { subscribeCookbooks } from '../features/cookbook/repository'
import type { Cookbook } from '../types/cookbook'

export function useCookbooks(): { cookbooks: Cookbook[]; loading: boolean } {
  const [cookbooks, setCookbooks] = useState<Cookbook[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsub = subscribeCookbooks((list) => {
      setCookbooks(list)
      setLoading(false)
    })
    return unsub
  }, [])

  return { cookbooks, loading }
}
