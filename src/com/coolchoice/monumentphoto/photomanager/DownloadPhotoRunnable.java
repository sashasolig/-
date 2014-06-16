package com.coolchoice.monumentphoto.photomanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.net.Uri;
import android.util.Log;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.data.Photo;
import com.coolchoice.monumentphoto.task.BaseTask;
import com.coolchoice.monumentphoto.task.LoginTask;
import com.coolchoice.monumentphoto.task.WebHttpsClient;

public class DownloadPhotoRunnable implements Runnable {

    private PhotoTask mDownloadPhotoTask;

    public DownloadPhotoRunnable(PhotoTask downloadPhotoTask) {
        this.mDownloadPhotoTask = downloadPhotoTask;
    }

    @Override
    public void run() {
        Uri destinationFileUri = this.mDownloadPhotoTask.getComplexGrave().generateFileUri(this.mDownloadPhotoTask.getPhoto().FileName);
        String uriString = destinationFileUri.toString();
        String thumbnailUriString = Photo.generateThumbnailUriString(uriString);
        String destinationUriString = null;
        switch (this.mDownloadPhotoTask.getTaskType()) {
        case ThreadManager.TASK_DOWNLOAD_IMAGE:
            destinationUriString = uriString;
            break;
        case ThreadManager.TASK_DOWNLOAD_THUMBNAIL:
            destinationUriString = thumbnailUriString;
            break;
        default:
            break;
        }
        destinationFileUri = Uri.parse(destinationUriString);
        String destinationFilePath = destinationFileUri.getPath();
        // http://192.168.53.11:8000/thumb/place-photos/2014/06/16/12/1402931941501.jpg/160x160~scale~secret.jpg
        //String url = "http://192.168.53.11:8000/media/place-photos/2014/02/27/2/140246885384.jpg";
        String suffix = String.format("/%dx%d~scale~secret.jpg", Settings.THUMBNAIL_SIZE, Settings.THUMBNAIL_SIZE);
        String url = String.format("%s%s%s", Settings.getPlacePhotoUrl(mDownloadPhotoTask.getContext()), mDownloadPhotoTask.getPhoto().ServerFileName, suffix);
        try {
            Log.i("Download photo start", String.format("url=%s destinationFilePath=%s", url, destinationFilePath));
            setDownloadStatus(ThreadManager.STATUS_DOWNLOAD_START);
            downloadImageFile(url, destinationFilePath);
            this.mDownloadPhotoTask.setResultUriString(destinationUriString);
            setDownloadStatus(ThreadManager.STATUS_DOWNLOAD_COMPLETE);
        } catch (IOException e) {
            setDownloadStatus(ThreadManager.STATUS_DOWNLOAD_ERROR);
            Log.e("IO", String.format("%s download error" , url), e);
        }        
    }
    
    private void setDownloadStatus(int status){
        this.mDownloadPhotoTask.setStatus(status);
        ThreadManager.getInstance().handleStatus(this.mDownloadPhotoTask, this.mDownloadPhotoTask.getStatus());
    }   
    
    protected void downloadImageFile(String urlString, String destinationFilePath) throws IOException {        
        HttpUriRequest httpGet = new HttpGet(urlString);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter("http.protocol.handle-redirects", false);
        httpGet.setParams(httpParams);
        httpGet.addHeader(BaseTask.HEADER_REFERER, urlString);
        HttpClient client = new DefaultHttpClient();
        if(WebHttpsClient.isHttps(urlString)){
            client = WebHttpsClient.wrapClient(client);
        }
        httpGet.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
        httpGet.addHeader(BaseTask.HEADER_AUTHORIZATION, String.format(BaseTask.HEADER_AUTHORIZATION_FORMAT_VALUE, Settings.getToken()));
        HttpResponse response = client.execute(httpGet);        
        InputStream inputStream = response.getEntity().getContent();
        try {
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            File destinationFile = new File(destinationFilePath);            
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destinationFilePath)));
            int inByte;
            while ((inByte = bis.read()) != -1 ) {
                bos.write(inByte);
            }
            bis.close();
            bos.close();
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            httpGet.abort();
            throw ex;
        } finally {
            inputStream.close();
        }        
    }

}
