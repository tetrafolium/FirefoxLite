/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.download;

import android.text.TextUtils;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by anlin on 27/07/2017.
 */

public class DownloadInfo {

// An overriding extra definition at the same level with DownloadManager.STATUS_SUCCESSFUL
// for the status field of this class which is an alternative when the
// DownloadManager.COLUMN_STATUS field is not available because no matching entry is available
// in the DownloadManager's table.
public static final int STATUS_DELETED = -1;

private Long RowId;
private Long DownloadId;
private int Status;
private String Size;
private String Date;
private String FileName = "";
private String MediaUri = "";
private String MimeType = "";
private String FileUri = "";
private String FileExtension = "";
private double sizeSoFar;
private double sizeTotal;
private boolean isRead;

public DownloadInfo() {
}

public void setRowId(Long id) {
	RowId = id;
}

public Long getRowId() {
	return RowId;
}

public void setFileExtension(String fileExtension) {
	if (!TextUtils.isEmpty(fileExtension)) {
		FileExtension = fileExtension;
	}
}

public String getFileExtension() {
	return FileExtension;
}

public void setStatusInt(int status) {
	Status = status;
}

public int getStatus() {
	return Status;
}

public void setFileUri(String fileUri) {
	if (!TextUtils.isEmpty(fileUri)) {
		FileUri = fileUri;
	}
}

public String getFileUri() {
	return FileUri;
}

public void setMimeType(String mimeType) {
	if (!TextUtils.isEmpty(mimeType)) {
		MimeType = mimeType;
	}
}

public String getMimeType() {
	return MimeType;
}

public void setMediaUri(String mediaUri) {
	if (!TextUtils.isEmpty(mediaUri)) {
		MediaUri = mediaUri;
	}
}

public String getMediaUri() {
	return MediaUri;
}

public void setDownloadId(Long downloadId) {
	DownloadId = downloadId;
}

public Long getDownloadId() {
	return DownloadId;
}

public void setFileName(String fileName) {

	if (!TextUtils.isEmpty(fileName)) {
		FileName = fileName;
	}
}

public String getFileName() {
	return FileName;
}

public void setSize(double size) {
	Size = convertByteToReadable(size);
}

public String getSize() {
	return Size;
}

public void setDate(long millis) {
	Date = convertMillis(millis);
}

public String getDate() {
	return Date;
}

private String convertMillis(long millis) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeInMillis(millis);
	return new Formatter().format("%tB %td", calendar, calendar).toString();
}

private String convertByteToReadable(double bytes) {
	String[] dictionary = {"bytes", "KB", "MB", "GB"};

	int index;
	for (index = 0; index < dictionary.length; index++) {
		if (bytes < 1024) {
			break;
		}
		bytes = bytes / 1024;
	}
	return String.format(Locale.getDefault(), "%.1f", bytes) + dictionary[index];
}

public boolean existInDownloadManager() {
	return getStatus() != STATUS_DELETED;
}

public void setSizeSoFar(double sizeSoFar) {
	this.sizeSoFar = sizeSoFar;
}

public double getSizeSoFar() {
	return sizeSoFar;
}

public void setSizeTotal(double sizeTotal) {
	this.sizeTotal = sizeTotal;
}

public double getSizeTotal() {
	return sizeTotal;
}

public boolean isRead() {
	return isRead;
}

public void setRead(boolean read) {
	isRead = read;
}
}
