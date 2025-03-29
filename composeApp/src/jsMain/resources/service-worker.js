self.addEventListener('install', (event) => {
  // 서비스 워커가 브라우저에 설치될 때 호출됩니다.
  // 만약 오프라인 캐시 기능을 원하지 않으면, 설치 이벤트에서 아무 작업을 하지 않아도 되지만,
  // 서비스 워커가 올바르게 설치되도록 install 이벤트를 정의하는 것이 일반적입니다.

  /* 캐쉬파일 등록 예시
  event.waitUntil(
      caches.open('my-pwa-cache').then((cache) => {
        return cache.addAll([
          '/',
          '/index.html',
          '/styles.css',
          '/composeResources/ggobong.composeapp.generated.resources/drawable/kcg-128x128.png'
        ]);
      })
    ); */

  console.log("Service Worker Installed");
});

self.addEventListener('fetch', (event) => {
  /* 캐쉬 사용 예시
  event.respondWith(
      caches.match(event.request).then((cachedResponse) => {
        return cachedResponse || fetch(event.request);
      })
    ); */
  event.respondWith(fetch(event.request));
});

self.addEventListener('activate', (event) => {
  // 서비스 워커가 새로 활성화될 때 호출됩니다.
  // 이 이벤트는 이전 버전의 서비스 워커 캐시를 정리할 때 유용하지만,
  // 현재 오프라인 캐시를 사용하지 않으므로 굳이 이전 캐시를 삭제할 필요는 없습니다.
  // 그러나 서비스 워커가 활성화될 때 확인하고 싶다면 이 이벤트를 정의할 수 있습니다.

  /* 캐쉬 정리 예시
    const cacheWhitelist = ['my-pwa-cache'];
    event.waitUntil(
      caches.keys().then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (!cacheWhitelist.includes(cacheName)) {
              return caches.delete(cacheName);
            }
          })
        );
      })
    ); */
  console.log("Service Worker Activated");
});
