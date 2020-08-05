package org.mozilla.cachedrequestloader;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.core.util.Pair;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(AndroidJUnit4.class)
public class BackgroundCachedRequestLoaderTest {

    private static final String PATH = "/path";
    private static final String RESPONSE_BODY = "body";
    private static final String RESPONSE_BODY2 = "body2";
    private static final int SOCKET_TAG = 1234;
    private static final String KEY = "KEY3";
    private static final String KEY2 = "KEY4";
    private int count = 0;

    @Test
    public void testLoadAndCacheLoadFaster() throws InterruptedException {
        final MockWebServer webServer = new MockWebServer();
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        try {
            webServer.enqueue(new MockResponse()
                              .setBody(RESPONSE_BODY)
                              .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
            webServer.enqueue(new MockResponse()
                              .setBody(RESPONSE_BODY2)
                              .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
            webServer.start();
        } catch (IOException e) {
            throw new AssertionError("Could not start web server", e);
        }
        final String targetUrl = webServer.url(PATH).toString();
        final Observer<Pair<Integer, String>> observer = s -> {
            count++;
            // First load from cache, cache does not exist yet, should be null.
            if (count == 1) {
                Assert.assertEquals(null, s.second);
            }
            // Load from Network
            if (count == 2) {
                Assert.assertEquals(RESPONSE_BODY, s.second);
                latch1.countDown();
            }
            // Second load from cache, cache should contain last element now.
            if (count == 3) {
                Assert.assertEquals(RESPONSE_BODY, s.second);
            }
            if (count == 4) {
                Assert.assertEquals(RESPONSE_BODY2, s.second);
                latch2.countDown();
            }
        };
        Context context = InstrumentationRegistry.getContext();
        BackgroundCachedRequestLoader cachedRequestLoader = new BackgroundCachedRequestLoader(context, KEY, targetUrl, null, SOCKET_TAG, false, true);
        cachedRequestLoader.deleteCache(context, KEY);
        LiveData<Pair<Integer, String>> liveData = cachedRequestLoader.getStringLiveData();
        liveData.observeForever(observer);
        latch1.await();
        // TODO: 11/5/18 This can be a source of intermittent.
        // Wait one second so the write to cache finishes.
        Thread.sleep(1000);
        BackgroundCachedRequestLoader urlSubscription2 = new BackgroundCachedRequestLoader(InstrumentationRegistry.getContext(), KEY, targetUrl, null, SOCKET_TAG, false, true);
        LiveData<Pair<Integer, String>> liveData2 = urlSubscription2.getStringLiveData();
        liveData2.observeForever(observer);
        // Wait until the test is fully finished.
        latch2.await();
    }

    @Test
    public void testLoadAndCacheCacheFaster() throws InterruptedException {
        final MockWebServer webServer = new MockWebServer();
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        try {
            webServer.enqueue(new MockResponse()
                              .setBody(RESPONSE_BODY)
                              .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
            webServer.enqueue(new MockResponse()
                              .setBody(RESPONSE_BODY2)
                              .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
            webServer.start();
        } catch (IOException e) {
            throw new AssertionError("Could not start web server", e);
        }
        final String targetUrl = webServer.url(PATH).toString();
        final Observer<Pair<Integer, String>> observer = s -> {
            count++;
            // Load from Network
            if (count == 1) {
                Assert.assertEquals(RESPONSE_BODY, s.second);
                latch1.countDown();
            }
            if (count == 2) {
                Assert.assertEquals(RESPONSE_BODY2, s.second);
                latch2.countDown();
            }
        };
        BackgroundCachedRequestLoader cachedRequestLoader = new BackgroundCachedRequestLoader(InstrumentationRegistry.getContext(), KEY2, targetUrl, null, SOCKET_TAG, true, false);
        LiveData<Pair<Integer, String>> liveData = cachedRequestLoader.getStringLiveData();
        liveData.observeForever(observer);
        latch1.await();
        // TODO: 11/5/18 This can be a source of intermittent.
        // Wait one second so the write to cache finishes.
        Thread.sleep(1000);
        BackgroundCachedRequestLoader urlSubscription2 = new BackgroundCachedRequestLoader(InstrumentationRegistry.getContext(), KEY2, targetUrl, null, SOCKET_TAG, true, false);
        LiveData<Pair<Integer, String>> liveData2 = urlSubscription2.getStringLiveData();
        liveData2.observeForever(observer);
        // Wait until the test is fully finished.
        latch2.await();
    }
}
