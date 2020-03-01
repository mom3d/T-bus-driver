package com.tunibus.api;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tunibus.tunibusdriver.R;

import java.util.List;

public class MyAdapter extends BaseAdapter{
    Context context;
    LayoutInflater inflater;
    List<LignesHoraires> lignesHoraires;

public MyAdapter(Context applicationContext ,List<LignesHoraires> ligneHoraires){
    this.context = applicationContext;
    this.lignesHoraires = ligneHoraires;
    this.inflater = (LayoutInflater.from(context));

}
    @Override
    public int getCount() {
        return lignesHoraires.size();
    }

    @Override
    public LignesHoraires getItem(int i) {
        return lignesHoraires.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.custom_spinner_item,null);
        TextView nomLigneView = view.findViewById(R.id.textView);
        TextView dataDepartView = view.findViewById(R.id.textView2);
        LignesHoraires mLigneHoraire = lignesHoraires.get(i);
        String nomLigne = mLigneHoraire.getNomLigne();
        String dateDepart = mLigneHoraire.getDateDepart();
        nomLigneView.setText(nomLigne);
        dataDepartView.setText(dateDepart);
        return view;
    }
}
