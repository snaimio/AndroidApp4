# Assignment 8 Feature Checklist - SuperPodcast

- [x] RSS feed parsing (`PodcastRssParser.kt`)
- [x] Episode model (`Episode.kt`)
- [x] Podcast detail screen (`PodcastDetailActivity.kt` & `activity_podcast_detail.xml`)
- [x] Episode list + adapter (`EpisodeAdapter.kt` & `item_episode.xml`)
- [x] ExoPlayer playback (audio/video/HLS via Media3)
- [x] Room DB + entity + DAO (`AppDatabase.kt`, `SubscribedPodcast.kt`, `SubscriptionDao.kt`)
- [x] Subscribe / unsubscribe toggle in detail screen
- [x] Subscriptions screen + adapter (`SubscriptionsActivity.kt`, `SubscriptionAdapter.kt`, `activity_subscriptions.xml`, `item_subscription.xml`)
- [x] Media type detection (`determinePodcastType`, `isVideoMedia`)
- [x] Notification channel + helper (`NotificationHelper.kt`)
- [x] Runtime notification permission (`POST_NOTIFICATIONS` on API 33+)
- [x] WorkManager scheduler + worker (`PodcastUpdateScheduler.kt`, `PodcastUpdateWorker.kt`)
- [x] New-episode detection (SharedPreferences GUID tracking)
- [x] Foreground toast / background notification
- [x] Notification tap → detail navigation with intent extras
