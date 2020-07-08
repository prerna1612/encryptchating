package com.example.encryptchatter;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private ListView listView;

    private DatabaseReference databaseReference;

    private String stringMessage;
    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);

        try{
            //it database reference which instance  get refered to
            databaseReference = FirebaseDatabase.getInstance().getReference("Message");

            try {
                //ciher encryped data to databse
                cipher = Cipher.getInstance("AES");
                //it decrypt data to database
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }

            secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
            //it creata channel to send strin transfer to database
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String[] stringMessageArray=null;
                    String[] stringFinal=null;
                    try {
                        //it gives th string to database
                        stringMessage = dataSnapshot.getValue().toString();
                        //updata string into substring
                        stringMessage = stringMessage.substring(1, stringMessage.length() - 1);
                        //it spilt the data and string
                        stringMessageArray = stringMessage.split(", ");
                        //it sort hrh array as date formatt
                        Arrays.sort(stringMessageArray);
                        //it continue string int doble how it store data nd time
                        stringFinal = new String[stringMessageArray.length*2];
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }



                    try {
                        // fooor loop for geting al array string's letter
                        for (int i = 0; i<stringMessageArray.length; i++) {
                            String[] stringKeyValue = stringMessageArray[i].split("=", 2);
                            stringFinal[2 * i] = (String) android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", Long.parseLong(stringKeyValue[0]));
                            stringFinal[2 * i + 1] = AESDecryptionMethod(stringKeyValue[1]);
                        }

                        //it list adpter to createa and adapter which store wthe string value to adapter
                        listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, stringFinal));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    //onclick button it stre and dend message to databse
    public void sendButton(View view){
        //for storing datefomatt createa an object of data class

        Date date = new Date();
        //on refeernce object get date and string which happedn on send button and send the string as argument od AESencryption method
        databaseReference.child(Long.toString(date.getTime())).setValue(AESEncryptionMethod(editText.getText().toString()));
        //it store an empty message which good in style(optional)
        editText.setText("");

    }
   //this method convert noraml messge to encryto format
    private String AESEncryptionMethod(String string){
    //pas string take and convert to an bytearray
        byte[] stringByte = string.getBytes();
        //creata nd array to store byte message which use after
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            //cipher object and inside cipher class convert the string to encryo
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            //after cipher do encrypt it ready to fianl decryption of stringbyte
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
//createa ad string which reurn
        String returnString = null;

        try {
            //and ecryed byte convert ot string formatt
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
       // necrupted value decryp after this(it thows exception on method)
        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        //it reutrn which sore last
        String decryptedString = string;
        //it store byte value after init
        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            //after decrpty decipher convert in to encryptbyte
            decryption = decipher.doFinal(EncryptedByte);
            //convert this to string
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }
}