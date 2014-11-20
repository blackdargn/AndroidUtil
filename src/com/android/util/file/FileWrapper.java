/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 文件包装器
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.android.util.system.Log;

/** URI文件包装器*/
public class FileWrapper
{
	private static final String TAG = "FileWrapper";
	/** 文件全路径*/
	private String path;
	/** 文件类型*/
	private String contentType;
	/** 文件简称*/
	private String src;
	/** 文件Uri*/
	private Uri uri;
	/** 文件大小*/
	private int size;

	public FileWrapper(Context context, Uri uri) throws Exception
	{
		if ((null == context) || (null == uri))
		{
			throw new IllegalArgumentException();
		}
		this.uri = uri;

		// 初始化文件大小
		initFileSize(context);
		// 初始化文件的路径与MIMI类型及简称
		String scheme = uri.getScheme();
		if (scheme.equals("content"))
		{
			initFromContentUri(context, uri);
		}
		else if (uri.getScheme().equals("file"))
		{
			initFromFile(context, uri);
		}
		src = path.substring(path.lastIndexOf('/') + 1);
		src = src.replace(' ', '_');
	}

	public String getPath()
	{
		return path;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getSrc()
	{
		return src;
	}

	public Uri getUri()
	{
		return uri;
	}

	public int getSize()
	{
		return size;
	}

	private void initFromFile(Context context, Uri uri)
	{
		path = uri.getPath();

		contentType = getContentType(path);
	}

	private void initFromContentUri(Context context, Uri uri)
	{
		Cursor c = context.getContentResolver().query(uri, null, null, null,
				null);

		if (c == null)
		{
			throw new IllegalArgumentException("Query on " + uri
					+ " returns null result.");
		}

		try
		{
			if ((c.getCount() != 1) || !c.moveToFirst())
			{
				throw new IllegalArgumentException("Query on " + uri
						+ " returns 0 or multiple rows.");
			}

			String filePath;

			if (uri.getAuthority().startsWith("mms"))
			{
				filePath = c.getString(c.getColumnIndexOrThrow("fn"));
				if (TextUtils.isEmpty(filePath))
				{
					filePath = c.getString(c.getColumnIndexOrThrow("_data"));
				}
				contentType = c.getString(c.getColumnIndexOrThrow("ct"));
			}
			else
			{
				filePath = c.getString(c
						.getColumnIndexOrThrow(Images.Media.DATA));
				contentType = c.getString(c
						.getColumnIndexOrThrow(Images.Media.MIME_TYPE));
			}
			path = filePath;
		}
		finally
		{
			c.close();
		}
	}

	private void initFileSize(Context context) throws Exception
	{
		ContentResolver cr = context.getContentResolver();
		InputStream input = null;
		try
		{
			input = cr.openInputStream(uri);
			if (input instanceof FileInputStream)
			{
				// avoid reading the whole stream to get its length
				FileInputStream f = (FileInputStream) input;
				size = (int) f.getChannel().size();
			}
			else
			{
				while (-1 != input.read())
				{
					size++;
				}
			}
		}
		catch (IOException e)
		{
			// Ignore
			Log.e(TAG, "IOException caught while opening or reading stream", e);
			if (e instanceof FileNotFoundException)
			{
				throw new Exception(e.getMessage());
			}
		}
		finally
		{
			if (null != input)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					// Ignore
					Log.e(TAG, "IOException caught while closing stream", e);
				}
			}
		}
	}

	/**
	 * 从本身媒体字典中获取文件的类型,找不到，则返回extension给其它方查找
	 * @param path
	 * @return MimeType 或者 extension
	 */
	public static String getContentType(String path)
	{
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String extension = MimeTypeMap.getFileExtensionFromUrl(path);
		if (TextUtils.isEmpty(extension))
		{
			// getMimeTypeFromExtension() doesn't handle spaces in filenames nor
			// can it handle
			// urlEncoded strings. Let's try one last time at finding the
			// extension.
			int dotPos = path.lastIndexOf('.');
			if (0 <= dotPos)
			{
				extension = path.substring(dotPos + 1);
			}
		}
		String contentType = mimeTypeMap.getMimeTypeFromExtension(extension);
		// It's ok if mContentType is null. Eventually we'll show a toast
		// telling the
		// user the picture couldn't be attached.
		if (contentType == null)
		{
			// 本身字典中找不到，就返回extension给其它查找
			return extension;
		}

		Log.d(TAG, "--->contentType: " + contentType);

		return contentType;
	}
}