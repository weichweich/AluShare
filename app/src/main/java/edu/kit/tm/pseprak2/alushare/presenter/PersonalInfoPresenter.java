package edu.kit.tm.pseprak2.alushare.presenter;


import android.widget.ImageView;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Profile;
import edu.kit.tm.pseprak2.alushare.view.PersonalInfoActivity;


public class PersonalInfoPresenter {
    private PersonalInfoActivity view;
    private String networkAdress = "Tor-ID noch nicht vorhanden";


    public PersonalInfoPresenter() {
    }

    public void onTakeView(PersonalInfoActivity view) {
        if(view == null){
            throw  new IllegalArgumentException();
        }
        this.view = view;
        if (Profile.getNetworkadress(view.getApplication()) != null) {
            this.networkAdress = Profile.getNetworkadress(view.getApplication());
        }
    }

    public void setQRCode() {
        QRCodeGenerator generator = new QRCodeGenerator(this.networkAdress);
        ((ImageView) this.view.findViewById(R.id.QRCode)).setImageBitmap(generator.generateQRCode());
    }

    public void setTorId() {
        ((TextView) this.view.findViewById(R.id.textViewTor)).setText(this.networkAdress);
    }

    public String getName() {
        return Profile.getOwnName(view.getApplication());
    }

}
