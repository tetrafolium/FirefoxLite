/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.download;

import static org.mozilla.focus.provider.DownloadContract.Download;

import android.app.DownloadManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.mozilla.focus.R;
import org.mozilla.focus.components.RelocateService;
import org.mozilla.focus.utils.CursorUtils;
import org.mozilla.focus.utils.IntentUtils;
import org.mozilla.rocket.util.LoggerWrapper;
import org.mozilla.threadutils.ThreadUtils;

/**
 * Created by anlin on 17/08/2017.
 */

public class DownloadInfoManager {

  public static final String DOWNLOAD_OPEN = "download open";
  public static final String ROW_ID = "row id";
  public static final String ROW_UPDATED = "row_updated";
  private static final int TOKEN = 2;
  private static DownloadInfoManager sInstance;
  private static Context mContext;
  private static DownloadInfoQueryHandler mQueryHandler;

  public static DownloadInfoManager getInstance() {
    if (sInstance == null) {
      sInstance = new DownloadInfoManager();
    }
    return sInstance;
  }

  private static final class DownloadInfoQueryHandler
      extends AsyncQueryHandler {

    public DownloadInfoQueryHandler(Context context) {
      super(context.getContentResolver());
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
      switch (token) {
      case TOKEN:
        if (cookie != null) {
          final long id =
              uri == null ? -1 : Long.parseLong(uri.getLastPathSegment());
          ((AsyncInsertListener)cookie).onInsertComplete(id);
        }
        break;
      default:
        break;
      }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
      switch (token) {
      case TOKEN:
        if (cookie != null) {
          AsyncDeleteWrapper wrapper = ((AsyncDeleteWrapper)cookie);
          if (wrapper.listener != null) {
            wrapper.listener.onDeleteComplete(result, wrapper.id);
          }
        }
        break;
      default:
        break;
      }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
      switch (token) {
      case TOKEN:
        if (cookie != null) {
          ((AsyncUpdateListener)cookie).onUpdateComplete(result);
        }
        break;
      default:
        break;
      }
    }

    @Override
    protected void onQueryComplete(int token, final Object cookie,
                                   final Cursor cursor) {
      switch (token) {
      case TOKEN:
        ThreadUtils.postToBackgroundThread(new Runnable() {
          @Override
          public void run() {
            if (cookie != null) {
              final List<DownloadInfo> downloadInfoList = new ArrayList<>();
              if (cursor != null) {
                try {
                  while (cursor.moveToNext()) {
                    final DownloadInfo downloadInfo =
                        cursorToDownloadInfo(cursor);
                    downloadInfoList.add(downloadInfo);
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                } finally {
                  CursorUtils.closeCursorSafely(cursor);
                }
              }

              ThreadUtils.postToMainThread(new Runnable() {
                @Override
                public void run() {
                  ((AsyncQueryListener)cookie)
                      .onQueryComplete(downloadInfoList);
                }
              });
            } else {
              CursorUtils.closeCursorSafely(cursor);
            }
          }
        });
        break;
      default:
        CursorUtils.closeCursorSafely(cursor);
        break;
      }
    }
  }

  private static final class AsyncDeleteWrapper {

    public long id;
    public AsyncDeleteListener listener;

    public AsyncDeleteWrapper(long id, AsyncDeleteListener listener) {
      this.id = id;
      this.listener = listener;
    }
  }

  public interface ContentChangeListener { void onContentChanged(); }

  public interface AsyncInsertListener { void onInsertComplete(long id); }

  public interface AsyncDeleteListener {
    void onDeleteComplete(int result, long id);
  }

  public interface AsyncUpdateListener { void onUpdateComplete(int result); }

  public interface AsyncQueryListener {
    void onQueryComplete(List<DownloadInfo> downloadInfoList);
  }

  public static void init(Context context) {
    mContext = context;
    mQueryHandler = new DownloadInfoQueryHandler(context);
  }

  public void insert(DownloadInfo downloadInfo, AsyncInsertListener listener) {
    mQueryHandler.startInsert(TOKEN, listener, Download.CONTENT_URI,
                              getContentValuesFromDownloadInfo(downloadInfo));
  }

  public void delete(Long rowId, AsyncDeleteListener listener) {
    mQueryHandler.startDelete(TOKEN, new AsyncDeleteWrapper(rowId, listener),
                              Download.CONTENT_URI, Download._ID + " = ?",
                              new String[] {Long.toString(rowId)});
  }

  public void updateByDownloadId(DownloadInfo downloadInfo,
                                 AsyncUpdateListener listener) {
    mQueryHandler.startUpdate(
        TOKEN, listener, Download.CONTENT_URI,
        getContentValuesFromDownloadInfo(downloadInfo),
        Download.DOWNLOAD_ID + " = ?",
        new String[] {Long.toString(downloadInfo.getDownloadId())});
  }

  public void updateByRowId(DownloadInfo downloadInfo,
                            AsyncUpdateListener listener) {
    mQueryHandler.startUpdate(
        TOKEN, listener, Download.CONTENT_URI,
        getContentValuesFromDownloadInfo(downloadInfo), Download._ID + " = ?",
        new String[] {Long.toString(downloadInfo.getRowId())});
  }

  public void query(int offset, int limit, AsyncQueryListener listener) {
    final String uri = Download.CONTENT_URI.toString() + "?offset=" + offset +
                       "&limit=" + limit;
    mQueryHandler.startQuery(TOKEN, listener, Uri.parse(uri), null, null, null,
                             Download._ID + " DESC");
  }

  public void queryByDownloadId(Long downloadId, AsyncQueryListener listener) {
    String uri = Download.CONTENT_URI.toString();
    mQueryHandler.startQuery(TOKEN, listener, Uri.parse(uri), null,
                             Download.DOWNLOAD_ID + "==?",
                             new String[] {Long.toString(downloadId)}, null);
  }

  public void queryByRowId(Long rowId, AsyncQueryListener listener) {
    String uri = Download.CONTENT_URI.toString();
    mQueryHandler.startQuery(TOKEN, listener, Uri.parse(uri), null,
                             Download._ID + "==?",
                             new String[] {Long.toString(rowId)}, null);
  }

  public void queryDownloadingAndUnreadIds(AsyncQueryListener listener) {
    final String uri = Download.CONTENT_URI.toString();
    mQueryHandler.startQuery(
        TOKEN, listener, Uri.parse(uri), null,
        Download.STATUS + "!=? or " + Download.IS_READ + "=?",
        new String[] {String.valueOf(DownloadManager.STATUS_SUCCESSFUL),
                      String.valueOf("0")},
        null);
  }

  public void markAllItemsAreRead(AsyncUpdateListener listener) {
    final ContentValues contentValues = new ContentValues();
    contentValues.put(Download.IS_READ, "1");
    mQueryHandler.startUpdate(
        TOKEN, listener, Download.CONTENT_URI, contentValues,
        Download.STATUS + "=? and " + Download.IS_READ + " = ?",
        new String[] {String.valueOf(DownloadManager.STATUS_SUCCESSFUL),
                      String.valueOf("0")});
  }

  public boolean recordExists(long downloadId) {
    final ContentResolver resolver = mContext.getContentResolver();
    final Uri uri = Download.CONTENT_URI;
    final String selection = Download.DOWNLOAD_ID + "=" + downloadId;
    final Cursor cursor = resolver.query(uri, null, selection, null, null);

    boolean isExist = false;
    try {
      isExist =
          (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      CursorUtils.closeCursorSafely(cursor);
    }

    return isExist;
  }

  /**
   * Update database, to replace file path of a record in both of our own db and
   * DownloadManager.
   *
   * @param downloadId download id for record in DownloadManager
   * @param newPath    new file path
   * @param type       Mime type
   */
  public void replacePath(final long downloadId, @NonNull final String newPath,
                          @NonNull final String type) {
    final long oldId = downloadId;
    final File newFile = new File(newPath);
    final DownloadPojo pojo = queryDownloadManager(mContext, downloadId);
    if (pojo == null) {
      // Should never happen
      final String msg = "File entry disappeared after being moved";
      throw new IllegalStateException(msg);
    }

    // remove old download from DownloadManager, then add new one
    // Description and MIME cannot be blank, otherwise system refuse to add new
    // record
    final DownloadManager manager =
        (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    final String desc =
        TextUtils.isEmpty(pojo.desc) ? "Downloaded from internet" : pojo.desc;
    final String mimeType = TextUtils.isEmpty(pojo.mime)
                                ? (TextUtils.isEmpty(type) ? "*/*" : type)
                                : pojo.mime;
    final boolean visible =
        true; // otherwise we need permission DOWNLOAD_WITHOUT_NOTIFICATION

    final long newId =
        manager.addCompletedDownload(newFile.getName(), desc, true, mimeType,
                                     newPath, newFile.length(), visible);

    // filename might be different from old file
    // update by row id
    queryByDownloadId(oldId, new AsyncQueryListener() {
      @Override
      public void onQueryComplete(List downloadInfoList) {
        for (int i = 0; i < downloadInfoList.size(); i++) {
          DownloadInfo queryDownloadInfo =
              (DownloadInfo)downloadInfoList.get(i);
          if (!queryDownloadInfo.existInDownloadManager()) {
            // Should never happen
            final String msg = "File entry disappeared after being moved";
            throw new IllegalStateException(msg);
          }
          if (oldId == queryDownloadInfo.getDownloadId()) {
            final long rowId = queryDownloadInfo.getRowId();
            final DownloadInfo newInfo =
                pojoToDownloadInfo(pojo, newPath, rowId);
            newInfo.setDownloadId(newId);
            updateByRowId(newInfo, new AsyncUpdateListener() {
              @Override
              public void onUpdateComplete(int result) {
                notifyRowUpdated(mContext, rowId);
                RelocateService.broadcastRelocateFinished(mContext, rowId);
              }
            });
            manager.remove(oldId);
            break;
          }
        }
      }
    });
  }

  public static void notifyRowUpdated(Context context, long rowId) {
    Intent intent = new Intent(DownloadInfoManager.ROW_UPDATED);
    intent.putExtra(DownloadInfoManager.ROW_ID, rowId);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }

  public DownloadManager getDownloadManager() {
    return (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
  }

  public void showOpenDownloadSnackBar(final Long rowId,
                                       @NonNull final View container,
                                       final String logTag) {
    queryByRowId(rowId, downloadInfoList -> {
      final boolean existInLocalDB = downloadInfoList.size() > 0;
      if (!existInLocalDB) {
        LoggerWrapper.throwOrWarn(
            logTag, "Download Completed with unknown local row id");
        return;
      }
      final DownloadInfo downloadInfo = downloadInfoList.get(0);
      final boolean existInDownloadManager =
          downloadInfo.existInDownloadManager();
      if (!existInDownloadManager) {
        LoggerWrapper.throwOrWarn(
            logTag, "Download Completed with unknown DownloadManager id");
      }
      String completedStr = container.getContext().getString(
          R.string.download_completed, downloadInfo.getFileName());
      final Snackbar snackbar =
          Snackbar.make(container, completedStr, Snackbar.LENGTH_LONG);
      // Set the open action only if we can.
      if (existInDownloadManager) {
        snackbar.setAction(
            R.string.open,
            view
            -> IntentUtils.intentOpenFile(container.getContext(),
                                          downloadInfo.getFileUri(),
                                          downloadInfo.getMimeType()));
      }
      snackbar.show();
    });
  }

  private static ContentValues
  getContentValuesFromDownloadInfo(DownloadInfo downloadInfo) {
    final ContentValues contentValues = new ContentValues();
    contentValues.put(Download.DOWNLOAD_ID, downloadInfo.getDownloadId());
    contentValues.put(Download.FILE_PATH, downloadInfo.getFileUri());
    contentValues.put(Download.STATUS, downloadInfo.getStatus());
    contentValues.put(Download.IS_READ, downloadInfo.isRead());

    return contentValues;
  }

  private static DownloadInfo cursorToDownloadInfo(Cursor cursor) {
    final long downloadId =
        cursor.getLong(cursor.getColumnIndex(Download.DOWNLOAD_ID));
    String fileUri =
        cursor.getString(cursor.getColumnIndex(Download.FILE_PATH));
    Long rowId = cursor.getLong(cursor.getColumnIndex(Download._ID));

    final DownloadPojo pojo = queryDownloadManager(mContext, downloadId);
    return pojo == null ? createEmptyDownloadInfo(downloadId, rowId, fileUri)
                        : pojoToDownloadInfo(pojo, fileUri, rowId);
  }

  private static DownloadInfo
  pojoToDownloadInfo(@NonNull final DownloadPojo pojo, final String fileUri,
                     long rowId) {
    final DownloadInfo info = new DownloadInfo();
    info.setRowId(rowId);
    info.setFileName(pojo.fileName);
    info.setDownloadId(pojo.downloadId);
    info.setSize(pojo.length);
    info.setSizeTotal(pojo.length);
    info.setSizeSoFar(pojo.sizeSoFar);
    info.setStatusInt(pojo.status);
    info.setDate(pojo.timeStamp);
    info.setMediaUri(pojo.mediaUri);
    info.setFileUri(pojo.fileUri);
    info.setMimeType(pojo.mime);
    info.setFileExtension(pojo.fileExtension);

    if (TextUtils.isEmpty(pojo.fileUri)) {
      info.setFileUri(fileUri);
    } else {
      info.setFileUri(pojo.fileUri);
    }

    return info;
  }

  private static DownloadInfo
  createEmptyDownloadInfo(long downloadId, long rowId, String fileUri) {
    final DownloadInfo info = new DownloadInfo();
    info.setRowId(rowId);
    info.setDownloadId(downloadId);
    info.setFileUri(fileUri);
    info.setStatusInt(DownloadInfo.STATUS_DELETED);
    return info;
  }

  @Nullable
  private static DownloadPojo
  queryDownloadManager(@NonNull final Context context, final long downloadId) {

    // query download manager
    final DownloadManager.Query query = new DownloadManager.Query();
    query.setFilterById(downloadId);
    final DownloadManager manager =
        (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    final Cursor managerCursor = manager.query(query);

    final DownloadPojo pojo = new DownloadPojo();
    pojo.downloadId = downloadId;
    try {
      if (managerCursor != null && managerCursor.moveToFirst()) {
        pojo.desc = managerCursor.getString(
            managerCursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
        pojo.status = managerCursor.getInt(
            managerCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        pojo.length = managerCursor.getDouble(managerCursor.getColumnIndex(
            DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        pojo.sizeSoFar = managerCursor.getDouble(managerCursor.getColumnIndex(
            DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        pojo.timeStamp = managerCursor.getLong(managerCursor.getColumnIndex(
            DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
        pojo.mediaUri = managerCursor.getString(managerCursor.getColumnIndex(
            DownloadManager.COLUMN_MEDIAPROVIDER_URI));
        pojo.fileUri = managerCursor.getString(
            managerCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        if (pojo.fileUri != null) {
          final String extension = MimeTypeMap.getFileExtensionFromUrl(
              URLEncoder.encode(pojo.fileUri, "UTF-8"));
          pojo.mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
              extension.toLowerCase(Locale.ROOT));
          pojo.fileExtension = extension;
          pojo.fileName = new File(Uri.parse(pojo.fileUri).getPath()).getName();
        }
      } else {
        // No pojo
        return null;
      }
    } catch (Exception e) {
      // No valid pojo
      return null;
    } finally {
      CursorUtils.closeCursorSafely(managerCursor);
    }

    return pojo;
  }

  /* Data class to store queried information from DownloadManager */
  private static class DownloadPojo {
    long downloadId;
    String desc;
    String mime;
    double length;
    double sizeSoFar;
    int status;
    long timeStamp;
    String mediaUri;
    String fileUri;
    String fileExtension;
    String fileName;
  }
}
