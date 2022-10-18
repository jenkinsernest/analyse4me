package view_model;

import android.annotation.SuppressLint;
import android.app.Application;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import com.jenkins.analyse4me.model.EventItem;
import com.jenkins.analyse4me.model.HeaderItem;
import com.jenkins.analyse4me.model.listItem;
import com.jenkins.analyse4me.model.messageModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


/*
@Author Ernest Jenkins

*/


public class brains extends AndroidViewModel {

    Application con;
    public MutableLiveData<List<listItem>> smsLiveData = new MutableLiveData<List<listItem>>();
    public int count=0;
    public int test_total=0;

    public EventHanlder handler;
    public MyAsyncTask async= new MyAsyncTask();
    public boolean empty_search_msg_shown=false;

    public BigDecimal sum_amount = new BigDecimal(0);
    public ContentResolver contentResolver;


    public interface EventHanlder {

        void show_loading();
        void close_loading();
        void show_msg_inter(String c);

    }

    public brains(Application con, EventHanlder handle) {
        super(con);
        this.con=con;
        this.handler = handle;
        contentResolver = con.getContentResolver();
    }



    public MutableLiveData<List<listItem>> getSms_data() {
        if (smsLiveData == null) {
            smsLiveData = new MutableLiveData<List<listItem>>();

        }
        
         async.execute();

        return smsLiveData;
    }


    public void drag_down_refresh(){
        new MyAsyncTask().execute();
    }


    public void refresh_data(){

   // System.out.println("async status view model =" + async.getStatus());
        if(async.getStatus()== AsyncTask.Status.FINISHED) {
new Thread(new Runnable() {
    @Override
    public void run() {
        getMessages();
    }
}).run();

        }
}

    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
          handler.show_loading();
        }

        @Override protected String doInBackground(Void... params) {
            getMessages();
            return null;
        }
        @Override protected void onPostExecute(String result) {


              handler.close_loading();


        }
    }





    public void getMessages(){

        String[] projection = new String[]{"_id","address", "person", "body", "date", "type"};
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                projection, null, null, "date DESC");

  LinkedHashMap<String,List<messageModel>> tree =new LinkedHashMap<String,List<messageModel>>();
  int totalSMS = cursor.getCount();



        try {
            if (cursor.moveToFirst()) { // must check the result to prevent exception



                messageModel msg = null;

                for (int idx = 0; idx < totalSMS; idx++) {
                    String date="";

                    List<messageModel> list = new ArrayList<messageModel>();


                    msg= read_filter_sms_body(cursor.getString(cursor.getColumnIndexOrThrow("body")), cursor);
                // String t=  Search(cursor.getString(
                  //          cursor.getColumnIndexOrThrow("address")).toString());
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

                    String _id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

                   String phone = cursor.getString(cursor.getColumnIndexOrThrow("address"));
System.out.println("type= " + type + " id=" +_id + " phone=" + phone);

                    // System.out.println("Reading = " + 1);


                    if(msg==null) {
                        // System.out.println("it is null ");
                    }
                    else{
                        date=msg.month;

 /*  if(msg.title.equalsIgnoreCase("Format Not Supported")
                           //  || msg.cost.equalsIgnoreCase("Format Not Supported")
   )
   {
//continue;
                        }
                        else {

*/
                            if (tree.containsKey(date)) {

                                list.addAll(tree.get(date));
                                list.add(msg);
                                //System.out.println("entered key= " + list.toString());

                                tree.put(date, list);

                            } else {
                                list.add(msg);
                                //System.out.println("entered= " + date);
                                tree.put(date, list);


                            }
                     //  }
                    } //else if ends


                    cursor.moveToNext();
                } //Loop ends


                 create_adaptor(tree);  //creates the data structure of list<listitem>

            } else {
                // empty box, no SMS
                System.out.println("empty indox");
                handler.show_msg_inter("empty indox");
            }

        }
        catch (Exception en){
            System.out.println(en.getMessage());
        }


    }






    void create_adaptor(Map<String,List<messageModel>> tree){
        count=0;
        sum_amount=new BigDecimal(0);
        List<listItem> mItems;

        mItems = new ArrayList<>();

        for (String date : tree.keySet()) {
            //System.out.println("date:" + date);
            HeaderItem header = new HeaderItem();
            header.setDate(date);
            mItems.add(header);

            count+=1;

            for (messageModel event : tree.get(date)) {
                EventItem item = new EventItem();
                item.setEvent(event);
                String[] separated=null;

                if(event.cost.equals("-- --")){

                }
                else {
                     separated = event.cost.split(" ");

                    try {
                        // test_total += Integer.parseInt(separated[1]);

                        BigDecimal amount = new BigDecimal(separated[1]);
                        //System.out.println("amount:" + amount);
                        sum_amount=  sum_amount.add(amount);

                    }catch (Exception d){

                    }

                }

                mItems.add(item);
            }
        }
        //System.out.println("sum:" + sum_amount);

        smsLiveData.postValue(mItems);

    }




public int find_cost_index(String[] data ){
        int v=-1;
 for(int a=0; a<=data.length-1; a++){

     //|| data[a].contains("NGN") || data[a].contains("")

     if(data[a].contains("AED") ){
         v= a;
     }
 }

 return v;
}



    public  messageModel read_filter_sms_body(String body, Cursor cursor){

        messageModel msg = null;
        String title, message, cost, date, month;
        int cost_idex=-1;
//|| body.contains(" ")
       // if(body.contains("AED") ) {


            String[] separated = null;
            try {
                separated = body.split("\n");

                if (separated != null ) {
                    if (separated.length > 2) {
                        cost_idex = find_cost_index(separated);
                    }
                }
            } catch (Exception d) {
                System.out.println("error = " + d.toString());
            }

          //  if (separated != null ) {
                // System.out.println("data lenght = " + separated.length);
if(cost_idex>-1) {
    title = Search(cursor.getString(
            cursor.getColumnIndexOrThrow("address")).toString());

    title = title.trim();

    if (title.equals(null) || title == null || title.equals("")) {
        title = cursor.getString(
                cursor.getColumnIndexOrThrow("address")).toString();
    }

    //else {
    //getTitle(separated[0]);
    message = body;

    if (cost_idex == -1) {
        cost = "-- --";
    } else {
        cost = getAmount(separated[cost_idex]);
    }
    date = getDate_data(
            getDate(cursor.getString(cursor.getColumnIndexOrThrow("date"))));

    month = getMonth(cursor.getString(cursor.getColumnIndexOrThrow("date")));

    msg = new messageModel(title, message, cost, date, month);
}
                        // System.out.println("title = " + title + "msg = " + message+  "cost = " + cost+
                        //        "date = " + date + "month =" + month );
                        return msg;
                   // }

           /* } else {
                return null;
            }*/
       /* }
        else{
            return null;
        }
*/
    }


    /* little utility functions*/




    @SuppressLint("Range")
    public String Search(String number) {

        String name = " ";

       // if(number.contains("+")){



        Uri uri = Uri.withAppendedPath(
               ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));


       ContentResolver contentResolver = con.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {
                        BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
                null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup
                        .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        }

        catch(Exception f) {
        }
        finally
         {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

       /* }
        else{
            name= number;
        }*/
        return name;
    }




    String getDate(String d){

        return new SimpleDateFormat("dd/M/yyyy", Locale.US).format(new Date(Long.parseLong(d)));
    }


    String getMonth(String date){

    String month = new SimpleDateFormat("MMMM", Locale.US).format(new Date(Long.parseLong(date)));


        return  month;
    }


    String getAmount(String body){
        // System.out.println("data = " + separated[0]);
        if(body.length()>1){
            if(body.contains("AED")) {
                return fix_amount_format(body);
            }
            else{
                return "Format Not Supported";
            }
        }
        else{
            return "Format Not Supported";
        }

    }


    String fix_amount_format(String am){
        String[] separated = am.split(" ");

        if(separated[0].contains("AED")) {
            return separated[0] + " " + separated[1];
        }
        else{
            return separated[1] + " " + separated[0];
        }
    }

    String getTitle(String body){
        // System.out.println("data = " + separated[0]);
        if(body.length()>1){
            return body;
        }
        else{
            return "Format Not Supported";
        }

    }




    String getDate_data( String dat){
        // System.out.println("data = " + separated[0]);
      /*  if(body.length()>2){

            if(isValidDate(body)) {
                return body;
            }
            else{*/
                return dat;
          /*  }
        }
        else{
            return dat;
        }*/

    }


    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    /* little utility functions ends*/

}
