package org.mozilla.lite.newspoint;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.lite.partner.Repository;

@RunWith(AndroidJUnit4.class)
public class RepositoryTest {

  
  
  

  @Test
  public void testParsing() throws InterruptedException {
    try {
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      
      final MockWebServer webServer = new MockWebServer();
      final InputStream inputStream =
          InstrumentationRegistry.getContext().getResources().openRawResource(
              org.mozilla.lite.newspoint.test.R.raw.response);
      final BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(inputStream));
      final String response = bufferedReader.readLine();
      webServer.enqueue(new MockResponse().setBody(response).addHeader(
          "Set-Cookie",
          "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
      webServer.start();
      
      countDownLatch.await();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
