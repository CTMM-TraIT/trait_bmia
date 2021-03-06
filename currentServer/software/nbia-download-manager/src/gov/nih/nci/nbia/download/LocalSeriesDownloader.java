package gov.nih.nci.nbia.download;

import gov.nih.nci.nbia.util.NBIAIOUtils;
import gov.nih.nci.nbia.util.StringUtil;
import gov.nih.nci.ncia.search.NBIANode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**This class downloads each series.
 *
 * @author lethai
 *
 */
public class LocalSeriesDownloader extends AbstractSeriesDownloader {

    /* Constructor for SeriesDownloader */
    public LocalSeriesDownloader() {

    }


    /**
     * {@inheritDoc}
     *
     * <p>Do a POST to the download servlet to retrieve the image files
     * (with authentication/authorization for non-public artifacts)
     */
    public void runImpl() throws Exception {
        this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);

        computeTotalSize();
        URL url = new URL(serverUrl);
        this.connectAndReadFromURL(url, 0);
    }


    /**
     * {@inheritDoc}
     */
    public NBIANode constructNode(String url, String displayName, boolean local) throws Exception {
        return new NBIANode(local, displayName, url);
    }

    ///////////////////////////////////////////PRIVATE//////////////////////////////////////

    /**
     * images already received... sent back to server for pause and resume
     * so server wont send these again.
     */
    private List<String> sopUidsList = new ArrayList<String>();

    private String createDirectory(){

        String localLocation = outputDirectory.getAbsolutePath() +
                               File.separator +
                               this.collection +
                               File.separator +
                               this.patientId +
                               File.separator +
                               this.studyInstanceUid +
                               File.separator +
                               this.modality +
                               File.separator +
                               this.seriesInstanceUid;
        File f = new File(localLocation);
        try{
            int count = 0;
            boolean mkdirResult=false;
            /* Attempt to create the directory again if it fails for 3 times.
             * The reason for this is because 3 threads are started by default,
             * sometime the directory is not created. We think it could be thread concurrency issue
             * with parent directories creation, but couldn't reproduce the problem.
             */
            while(count < 3){
               if(!f.exists()){
                    mkdirResult = f.mkdirs();
                    if(mkdirResult==false) {
                        System.out.println("couldnt create directory: " + localLocation + " \tattempt number: " + count);
                        count++;
                   }else{
                        break;
                   }
               }else{
            	   mkdirResult=true;
            	   break;
               }
            }
            if(!mkdirResult){
                throw new RuntimeException("Could not create directory after 3 attempts: " + localLocation);
        }
        }catch(SecurityException se){
            throw new RuntimeException("SecurityException on creating directory: "+localLocation + "  " +se);
        }
        return localLocation;
    }

    private void connectAndReadFromURL(URL url, int attempt) throws Exception {
        System.out.println("attempt" + attempt +" for the series" +this.seriesInstanceUid);
        if(attempt >= noOfRetry) {
          additionalInfo.append("Reached max retry ("+ noOfRetry+") attempts.\n");
          System.out.println(additionalInfo);
          error();
          return;
        }
        TrustStrategy easyStrategy = new TrustStrategy() { 
            @Override 
            public boolean isTrusted(X509Certificate[] certificate, String authType) 
                    throws CertificateException { 
                return true; 
            } 
        };
        // set up a TrustManager that trusts everything 
        SSLSocketFactory sslsf = new SSLSocketFactory(easyStrategy,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme httpsScheme = new Scheme("https", 443, sslsf); 
        SchemeRegistry schemeRegistry = new SchemeRegistry(); 
        schemeRegistry.register(httpsScheme); 
        schemeRegistry.register(new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));
        HttpParams httpParams = new BasicHttpParams(); 
        // set timeout values     
        HttpConnectionParams.setConnectionTimeout(httpParams, 50000); 
        HttpConnectionParams.setSoTimeout(httpParams,  new Integer(12000));
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(schemeRegistry); 
        DefaultHttpClient httpClient = new DefaultHttpClient(ccm,httpParams);

        //Additions by lrt for tcia - 
        //attempt to reduce errors going through a Coyote Point Equalizer load balance switch 
        httpClient.getParams().setParameter("http.socket.timeout", new Integer(12000));
        httpClient.getParams().setParameter("http.socket.receivebuffer", new Integer(16384));
        httpClient.getParams().setParameter("http.tcp.nodelay", true);
        httpClient.getParams().setParameter("http.connection.stalecheck", false);
        // end lrt additions
     
        HttpPost httpPostMethod = new HttpPost(url.toString());
        List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>(); 
        postParams.add(new BasicNameValuePair("userId", this.userId)); 
        postParams.add(new BasicNameValuePair("sopUids", this.sopUids));
        postParams.add(new BasicNameValuePair("seriesUid", this.seriesInstanceUid));
        postParams.add(new BasicNameValuePair("includeAnnotation", Boolean.toString(this.includeAnnotation)));
        postParams.add(new BasicNameValuePair("hasAnnotation", Boolean.toString(this.hasAnnotation)));
        //this is ignored
        postParams.add(new BasicNameValuePair("Range", "bytes=" + downloaded + "-"));
        postParams.add(new BasicNameValuePair("password", password));
        
        httpPostMethod.addHeader("password",	password);
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() { 
            @Override
            public boolean retryRequest(IOException exception,int executionCount,HttpContext context) { 
                if (executionCount >= noOfRetry) { 
                    // Do not retry if over max retry count 
                    System.out.println("no retring since reached max retry attempt,"+ noOfRetry +", for series-" +  seriesInstanceUid);
                    additionalInfo.append("Reached max retry ("+ noOfRetry+") attempts using retry handler.\n");
                    return false; 
                } 
                if (exception instanceof NoHttpResponseException) {
                    // Retry on when server dropped connection
                    System.out.println("NoHttpResponseException exception- Retry if the server dropped connection on us "); 
                    return true; 
                } 
                if (exception instanceof SSLHandshakeException) { 
                    // Do not retry on SSL handshake exception 
                    System.out.println("SSLHandshakeException exception - no retry"); 
                    return false; 
                } 
                if (exception instanceof java.net.SocketTimeoutException) { 
                  // Retry on socket timeout exception 
                  System.out.println("java.net.SocketTimeoutException exception- retry the series" +  seriesInstanceUid);
                  additionalInfo.append(" retry handler attempt").append(executionCount).append(" for SocketTimeOutException \n");;
                  return true; 
                } 
                if (exception instanceof java.net.SocketException) { 
                    // Retry on socket timeout exception 
                    System.out.println("java.net.SocketException - retry the series" +  seriesInstanceUid);
                    additionalInfo.append(" retry handler attempt").append(executionCount).append(" for SocketException \n");
                    return true; 
                  } 
                HttpRequest request = (HttpRequest) context.getAttribute( ExecutionContext.HTTP_REQUEST); 
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);  
                if (idempotent) { 
                    System.out.println("idempotent exception - Retry"); 
                    // Retry if the request is considered idempotent  
                    return true; 
                } 
                return false; 
            }
        }; 
        httpClient.setHttpRequestRetryHandler(myRetryHandler); 
        UrlEncodedFormEntity query = new UrlEncodedFormEntity(postParams);  
        httpPostMethod.setEntity(query); 
        HttpResponse response = httpClient.execute(httpPostMethod); 

        /* Connect to server. */
        try {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("response code: " + responseCode + "for the seriers " + this.seriesInstanceUid);

            /* Make sure response code is in the 200 range.*/
            if (responseCode / 100 != 2) {
                //additionalInfo.append("incorrect response code");
                System.out.println("retry the rest of images from series as there was incorrect response code for the seriers " + this.seriesInstanceUid);
                additionalInfo.append(" retry attempt").append(attempt+1).append("for incorrect response code \n");
                connectAndReadFromURL(url, attempt+1);
            }

            /* Set the size for this download if it
             * hasn't been already set. */
            if (size == -1) {
                System.out.println("no data found for the series");
                status = NO_DATA;
                stateChanged();
             }
             readFromConnection(response.getEntity().getContent());
        } catch (SocketTimeoutException e) {
            //exclude downloading already download image.
            this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
            System.out.println("retry the rest of images from series as there was Socket timeout exception.........");
            e.printStackTrace();
            additionalInfo.append(" retry image attempt").append(attempt+1).append(" for Socket timeout exception \n");
            connectAndReadFromURL(url, attempt+1);
        } catch (SocketException e) {
            //exclude downloading already download image.
            this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
            System.out.println("retry the rest of images from series as there was Socket exception.........");
            e.printStackTrace();
            additionalInfo.append(" retry image attempt").append(attempt+1).append(" for Socket exception \n");
            connectAndReadFromURL(url, attempt+1);
        }
        finally {
        	httpClient.getConnectionManager().shutdown(); 
        }
    }

    private void readFromConnection(InputStream is) throws Exception {
        String location = createDirectory();

        TarArchiveInputStream zis = new TarArchiveInputStream(is);

        int imageCnt = 0;
        // Begin lrt additions
        imageCnt = sopUidsList.size(); // needed for pause/resume, and for error recovery
        int downloadedImgSize = 0;
        int downloadedAnnoSize = 0;
        // End lrt additions
        try {
        	//the pause button affects this loop
        	//per tar entry, will check status... which pause button could
        	//change... then we will drop out of downloading.
            while(status == DOWNLOADING) {
                TarArchiveEntry tarArchiveEntry = zis.getNextTarEntry();
                if(tarArchiveEntry==null) {
                    System.out.println(this.seriesInstanceUid+" reaching end of file");
                    status = COMPLETE;
                    break;
                }
                // Begin lrt additions
                long fileSize = tarArchiveEntry.getSize();
                int startDownloaded = downloaded;
                // End lrt additions
                String sop = tarArchiveEntry.getName();
                int pos = sop.indexOf(".dcm");
                OutputStream outputStream = null;
                if(pos > 0){
                    // sopUidsList.add(sop.substring(0, pos)); - lrt moved to below, after file size check
                    outputStream = new FileOutputStream(location  + File.separator + StringUtil.displayAsSixDigitString(imageCnt)+".dcm");
                }
                else {
                    outputStream = new FileOutputStream(location  + File.separator + sop);
                }

                try {
                    NBIAIOUtils.copy(zis, outputStream, progressUpdater);
                }
                finally {
                    outputStream.flush();
                    outputStream.close();
                }

                imageCnt += 1;
                // 	Begin lrt additions
                int bytesDownloaded = downloaded - startDownloaded;
                if (bytesDownloaded != fileSize) {
                   additionalInfo.append(" file size mismatch for instance "+sop +"\n");
                   System.out.println(this.seriesInstanceUid + additionalInfo);
                   error();
                }
                else {
                    if(pos > 0){ // image file
                        downloadedImgSize += bytesDownloaded;
                        sopUidsList.add(sop.substring(0, pos));
                    }
                    else { // annotation file
                         downloadedAnnoSize += bytesDownloaded;
                    }
                }
                // End lrt additions
            }                    
        }
        finally {
            org.apache.commons.io.IOUtils.closeQuietly(zis);
        }
        // Begin lrt additions
        if (status == COMPLETE) {
            if (sopUidsList.size() != Integer.valueOf(numberOfImages).intValue()) {
                additionalInfo.append("Number of image files mismatch. It Was supposed to be "+numberOfImages +" image files but we found "+sopUidsList.size()+" at server side\n");
                System.out.println( this.seriesInstanceUid + additionalInfo);
                error();
            }
			/* Cannot use this sanity check, since image sizes are not always recorded correct in NBIA 5.0 - lrt
			else if (downloadedImgSize != imagesSize) {
				System.out.println(this.seriesInstanceUid+" total size of image files mismatch.  Was "+downloadedImgSize+" should be "+imagesSize);
				error();
			}
			
            else if (downloadedAnnoSize != annoSize) {
                additionalInfo.append(" total size of annotation files mismatch.  Was "+downloadedAnnoSize+" should be "+annoSize+"\n");
                System.out.println(this.seriesInstanceUid + additionalInfo);
                error();
            }*/
        }
        // End lrt additions
    }


//    static {
//        Protocol.registerProtocol("https",
//                 new Protocol("https",
//                 new EasySSLProtocolSocketFactory(),
//                 443));
//    }
    
}