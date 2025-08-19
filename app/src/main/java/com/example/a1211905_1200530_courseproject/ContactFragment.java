package com.example.a1211905_1200530_courseproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ContactFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        TextView titleText = view.findViewById(R.id.titleText);
        Button callButton = view.findViewById(R.id.callButton);
        Button locateButton = view.findViewById(R.id.locateButton);
        Button emailButton = view.findViewById(R.id.emailButton);

        titleText.setText(getString(R.string.contact_title));

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + getString(R.string.store_phone)));
                startActivity(intent);
            }
        });

        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Google Maps to BZU
                Uri gmmIntentUri = Uri.parse("geo:31.9539,35.1886?q=Birzeit+University");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Fallback to any map app
                    Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(intent);
                }
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + getString(R.string.store_email)));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Contact from Android Grocery App");
                startActivity(Intent.createChooser(intent, "Send email"));
            }
        });

        return view;
    }
}