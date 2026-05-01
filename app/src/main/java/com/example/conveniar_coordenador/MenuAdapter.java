package com.example.conveniar_coordenador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MenuAdapter extends ArrayAdapter<ItemMenu> {

    public MenuAdapter(Context context, List<ItemMenu> lista) {
        super(context, 0, lista);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_lista, parent, false);
        }

        ItemMenu item = getItem(position);

        ImageView img = convertView.findViewById(R.id.img_icon);
        TextView txt = convertView.findViewById(R.id.txt_nome);

        img.setImageResource(item.icone);
        txt.setText(item.nome);

        return convertView;
    }
}
