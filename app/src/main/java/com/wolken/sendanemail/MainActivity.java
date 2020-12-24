package com.wolken.sendanemail;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.wolken.sendanemail.google.GmailCredentials;
import com.wolken.sendanemail.google.GmailService;
import com.wolken.sendanemail.google.GmailServiceImpl;
import com.wolken.sendanemail.outlook.SendEmailService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import co.nedim.maildroidx.MaildroidX;
import co.nedim.maildroidx.MaildroidXType;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static javax.mail.Message.RecipientType.TO;

public class MainActivity extends AppCompatActivity {
    private static final String APP_TAG = "app_tag";
    public String photoFileName = "photo.jpg";
    File photoFile;
    private EasyImage easyImage;

    private EditText mFromAddress, mToAddress, mSubject, mCompose;
    private Button mSendEmail;
    String fromAddress = null;
    String toAddress = null;
    String subject = null;
    String body = null;
    ImageView mImageAttached;
    String clientId = "65329882926-u9vds22lcndk5r3821qqontgv70ppmrr.apps.googleusercontent.com";
    Handler mHandler;
    TextView mAttachedName;
    Uri attachedUri;
    ProgressDialog mProgressDialog;
    Button btAttach;
    int pickFromGallery =  101;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        mAttachedName = findViewById(R.id.tv_attached_name);
        btAttach = findViewById(R.id.attach);
        mFromAddress = findViewById(R.id.ed_from_address);
        mToAddress = findViewById(R.id.ed_to_address);
        mSubject = findViewById(R.id.ed_subject);
        mCompose = findViewById(R.id.ed_compose);
        mSendEmail = findViewById(R.id.button);
        mImageAttached = findViewById(R.id.iv_img_attached);
        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick media")
                .setCopyImagesToPublicGalleryFolder(false)
//                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(false)
                .build();
        btAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Dexter.withContext(MainActivity.this)
                        .withPermissions(Manifest.permission.CAMERA)
                        .withListener(new MultiplePermissionsListener() {

                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                easyImage.openCameraForImage(MainActivity.this);
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();

                //openFolder();

            }
        });
        mFromAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                fromAddress = editable.toString();
            }
        });

        mToAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                toAddress = editable.toString();
            }
        });

        mSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                subject = editable.toString();
            }
        });

        mCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                body = editable.toString();
            }
        });


        mSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler = new Handler();

                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mProgressDialog = new ProgressDialog(MainActivity.this);
                        mProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait", "Sending mail", true, false);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        //sendPlainTextEmail("smtp.gmail.com", "587", fromAddress, "Mcafee@123", toAddress, subject, body);
                         /*send an email through outlook services within the app without attachment*/
                        //sendPlainTextEmailOutlook("smtp.office365.com","587",fromAddress,"Pwd@12345",toAddress,subject,body)
                        /*send an email through outlook services within the app with attachment*/
                        /*Bitmap bm  = BitmapFactory.decodeResource(getResources(),R.drawable.lord_venkateshwara);
                        Log.d("bm",""+bm);
                        new SendEmailService(MainActivity.this,fromAddress,"Pwd@12345").SendEmail(bm,fromAddress,toAddress,subject,body);*/
                        composeEmail(new String[]{toAddress},subject,body);
                        //composeEmailOutlook(toAddress,subject,body);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if(mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Email Sent!...", Toast.LENGTH_SHORT).show();
                                mToAddress.setText("");
                                mFromAddress.setText("");
                                mSubject.setText("");
                                mCompose.setText("");
                            }
                        });

                    }
                }.execute();
               /* mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                });*/

            }
        });


    }

    private void openFolder() {
        Intent intent = new Intent(); intent.setType("image/*");intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data",true);

        startActivityForResult(Intent.createChooser(intent, "Complete action using"), pickFromGallery);
    }

    public void sendPlainTextEmail(String host, String port,
                                   final String userName, final String password, String toAddress,
                                   String subject, String message) throws AddressException,
            MessagingException {

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
// *** BEGIN CHANGE
        properties.put("mail.smtp.user", userName);

        // creates a new session, no Authenticator (will connect() later)
        Session session = Session.getDefaultInstance(properties);
// *** END CHANGE

        // creates a new e-mail message
        javax.mail.Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        // set plain text message
        msg.setText(message);

// *** BEGIN CHANGE
        // sends the e-mail
        Transport t = session.getTransport("smtp");
        t.connect(userName, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();

// *** END CHANGE

    }

    public void sendPlainTextEmailOutlook(String host, String port,
                                   final String userName, final String password, String toAddress,
                                   String subject, String message) throws AddressException,
            MessagingException {

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

       properties.put("mail.smtp.ssl.trust", "smtp.office365.com");
// *** BEGIN CHANGE
        properties.put("mail.smtp.user", userName);

        // creates a new session, no Authenticator (will connect() later)
        Session session = Session.getDefaultInstance(properties);
// *** END CHANGE

        // creates a new e-mail message
        javax.mail.Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        // set plain text message
        msg.setText(message);

// *** BEGIN CHANGE
        // sends the e-mail
        Transport t = session.getTransport("smtp");
        t.connect(userName, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();

// *** END CHANGE

    }

    public void sendEmailWithAttachment(String host, String port,
                                        final String userName, final String password, String toAddress,
                                        String subject, String message){

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pickFromGallery && resultCode == RESULT_OK){
            if(data!=null){
                attachedUri = data.getData();
            }
            mAttachedName.setText(attachedUri.getLastPathSegment());
        }
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            Log.d("BitMap",""+bitmap);
            mImageAttached.setImageBitmap(bitmap);
            try {

                File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()){
                    photoFile = new File(root, "pic.png");
                    FileOutputStream out = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                for (MediaFile imageFile : imageFiles) {
                    Log.d("EasyImage", "Image file returned: " + imageFile.getFile().toString() +"URI"+ imageFile.getUri$library_release());
                    Uri uri = Uri.fromFile(imageFile.getFile());
                    attachedUri = imageFile.getUri$library_release();
                    photoFile = imageFile.getFile();
                    mImageAttached.setImageURI(uri);
                }

            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.wolken.sendanemail.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                attachedUri = photoURI;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }*/
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
    /*Intent for sending an email with attachment*/
    public void composeEmail(String[] addresses, String subject,String body) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachedUri);
        intent.putExtra(Intent.EXTRA_TEXT,body);
        intent.setType("image/png");
        //intent.setType("message/rfc822");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            //startActivity(Intent.createChooser(intent,"Choose Outlook app"));
        }
    }
    public void composeEmailOutlook(String[] addresses,String subject,String body){

        String uriText =
                "mailto:"+addresses +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode(body);

        Uri uri = Uri.parse(uriText);

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(uri);
        startActivity(Intent.createChooser(sendIntent, "Send email"));
    }


}