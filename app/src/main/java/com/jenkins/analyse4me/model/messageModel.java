package com.jenkins.analyse4me.model;



/*
@Author Ernest Jenkins

*/
public class messageModel {


    public String message;

        public String title;


        public String cost;


        public String date;
        public String month;

       public messageModel(String title,String message, String cost,String date, String month){

          this.title=title;
          this.message=message;
          this.date=date;
          this.cost=cost;
          this.month=month;
       }



}