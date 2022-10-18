package com.jenkins.analyse4me.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jenkins.analyse4me.model.EventItem;
import com.jenkins.analyse4me.model.HeaderItem;
import com.jenkins.analyse4me.R;
import com.jenkins.analyse4me.model.listItem;
import com.jenkins.analyse4me.model.messageModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


/*
@Author Ernest Jenkins

*/
public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements Filterable {
    private List<listItem> responseList;
    private List<listItem> contactListFiltered;
    private Context context;


    int total_count;

    private BigDecimal Search_sum_amount= new BigDecimal(0);

    DecimalFormat formatter = new DecimalFormat("#,###,###");

    Map<String,List<messageModel>> tree =new HashMap<String,List<messageModel>>();
    LinkedHashSet<String> hash= new LinkedHashSet<String>();
    List<listItem> mItems= new ArrayList<>();

    TextView total_month;
    TextView total_amount;



    public EventHanlder handler;


    public interface EventHanlder {
        void Update_ui();

        public void Update_ui_pos(int count, BigDecimal amount);
    }



public Adapter(Context context, List<messageModel> reportList, LinkedHashSet<String> hash, List<listItem> mItems,
                   TextView total_month , TextView total_amount , int count,  EventHanlder handler) {

this.total_count=count;
this.context = context;
this.hash= hash;
this.mItems= mItems;
this.contactListFiltered= mItems;
this.total_month = total_month;
this.total_amount = total_amount;
this.handler = handler;
    }



    @Override
    public int getItemViewType(int position) {
        return contactListFiltered.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=null;

        if (viewType == listItem.TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header,
                    parent, false);
            return new HeaderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item_list,
                    parent, false);
            return new ReportViewHolder(view);
        }

       // return new ReportViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        //viewHolder.setIsRecyclable(false);

        int type = getItemViewType(position);
        if (type == listItem.TYPE_HEADER) {
            HeaderItem header = (HeaderItem) contactListFiltered.get(position);
            HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            // your logic here
            holder.date.setText(header.getDate());
        }

        else if(type == listItem.TYPE_EVENT) {
            EventItem event = (EventItem) contactListFiltered.get(position);
            ReportViewHolder holder = (ReportViewHolder) viewHolder;
            // your logic here

                holder.date.setText(event.getEvent().date);
                holder.title.setText(event.getEvent().title);
                holder.cost.setText(event.getEvent().cost);
                holder.msg.setText(event.getEvent().message);

        }

    }



    @Override
    public int getItemCount() {
        int dat=contactListFiltered.size() ;
        return dat;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {


            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                int count=0;


                if (charString.isEmpty()) {
                    contactListFiltered = mItems;

                    handler.Update_ui();
                } else {

                    List<listItem> filteredList = new ArrayList<>();
                    boolean status=true;

                    for (listItem row : mItems) {

                        count+=1;
                        EventItem event=null ;
                        HeaderItem header=null ;

                        if(row.getType()==listItem.TYPE_HEADER) {
                    if(filteredList.size()>0){
                        if(filteredList.get(filteredList.size()-1).getType()==listItem.TYPE_HEADER){
                            filteredList.remove(filteredList.size()-1);
                        }
                    }
                                header = (HeaderItem) row;
                            filteredList.add(header);

                        }
                        else if(row.getType()==listItem.TYPE_EVENT) {
                          event = (EventItem) row;

                            if (event.getEvent().title.toLowerCase().startsWith(charString.toLowerCase())
                            ) {
                               // System.out.println("Found "+ event.getEvent().title + "against=" + charString);



                                filteredList.add(event);


                            }
                            else{
                                if(mItems.size()-1==count){
                                   if( filteredList.get(filteredList.size()-1).getType()==listItem.TYPE_HEADER){
                                       filteredList.remove(filteredList.size()-1);
                                   }
                                }
                            }


                        }

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name  match

                    }

                   // System.out.println("Found "+ filteredList);

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //contactListFiltered.clear();
                Search_sum_amount=new BigDecimal(0);

                contactListFiltered = (ArrayList<listItem>) filterResults.values;

                int month_count=0;
                int total=0;
                //System.out.println("Found2 "+ contactListFiltered);


                for(listItem d: contactListFiltered){

                    if(d.getType()==listItem.TYPE_HEADER){

                        month_count+=1;
                    }
                    else{
                            try {
                                EventItem event = (EventItem) d;

                                if(event.getEvent().cost.equals("-- --")){

                                }
                                else {
                                    String[] separated = event.getEvent().cost.split(" ");
                                    int am = Integer.parseInt(separated[1]);

                                    BigDecimal amount = new BigDecimal(separated[1]);
                                    Search_sum_amount = Search_sum_amount.add(amount);

                                    total += am;
                                }
                            } catch (Exception f) {
                                System.out.println("exception= " + f.toString());
                            }

                    }
                }
               // System.out.println("total= " +total);

                handler.Update_ui_pos(month_count,Search_sum_amount);


                notifyDataSetChanged();
            }
        };
    }



    class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private TextView cost;
        private TextView date;
        private TextView msg;
       RelativeLayout month_lay;
       TextView month;
        String month_data="jan";
    CardView card;

        public ReportViewHolder(@NonNull View itemView
                                ) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            title = itemView.findViewById(R.id.name);
            cost = itemView.findViewById(R.id.amount);
            msg = itemView.findViewById(R.id.msg);
            date = itemView.findViewById(R.id.date);
            month = itemView.findViewById(R.id.month);
           // month_lay = itemView.findViewById(R.id.month_layout);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //reportInteractionListener.onReportClicked(getAdapterPosition(), responseList.get(getAdapterPosition()));
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private TextView date;



        public HeaderViewHolder(@NonNull View itemView
                                ) {
            super(itemView);


            date = itemView.findViewById(R.id.month);



            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //reportInteractionListener.onReportClicked(getAdapterPosition(), responseList.get(getAdapterPosition()));
        }
    }




}
