package com.example.matanghuy.mizorem.editevent.attendees;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.matanghuy.mizorem.R;

import java.util.List;

/**
 * Created by matanghuy on 2/28/15.
 */
public class AttendeesListAdapter extends ArrayAdapter<Attendee> {
    private Context context;

    public AttendeesListAdapter(Context context,List<Attendee> items) {
        super(context, R.layout.atendee_list_item, items);
        this.context = context;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        TextView attendeeName;
        ImageView attendeeImage;
        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.atendee_list_item, parent, false);
            attendeeName = (TextView) convertView.findViewById(R.id.tvAttendeeName);
            attendeeImage = (ImageView) convertView.findViewById(R.id.ivAttendeePicture);
            convertView.setTag(new ViewHolder(attendeeImage, attendeeName));
        } else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            attendeeName = viewHolder.attendeeName;
            attendeeImage = viewHolder.attendeeImage;
        }

        Attendee attendee = getItem(position);
        if(attendee.isAdmin()) {
            attendeeName.setText(attendee.getName() + " (Admin)");
        } else {
            attendeeName.setText(attendee.getName());
        }
        attendeeImage.setImageResource(R.drawable.avatar);

        return convertView;



    }

    private static class ViewHolder {
        public final ImageView attendeeImage;
        public final TextView attendeeName;

        public ViewHolder(ImageView attendeeImage, TextView attendeeName) {
            this.attendeeImage = attendeeImage;
            this.attendeeName = attendeeName;
        }
    }


}

