package com.jenkins.analyse4me.model;

/*
@Author Ernest Jenkins

*/
public class HeaderItem extends listItem {

        private String date;
        private int sum=0;
        // here getters and setters
        // for title and so on, built
        // using date
public void setDate(String d){
    this.date=d;
    this.sum+=1;
        }
        @Override
        public int getType() {
            return TYPE_HEADER;
        }

        public String getDate() {
            return date;
        }

        public int getSum() {
            return sum;
        }
    }

