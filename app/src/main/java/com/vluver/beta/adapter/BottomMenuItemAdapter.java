package com.vluver.beta.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.vluver.beta.fragments.Avisos;
import com.vluver.beta.fragments.Inicio;
import com.vluver.beta.fragments.Mapa;
import com.vluver.beta.fragments.Notificaciones;

import java.util.ArrayList;

/**
 * Created by Vlover on 15/04/2018.
 */

public class BottomMenuItemAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    private Fragment currentFragment;
    private int lastPosition = -1;

    public BottomMenuItemAdapter(FragmentManager fm) {
        super(fm);

        fragments.add(new Inicio());
        fragments.add(new Avisos());
        fragments.add(new Mapa());
        fragments.add(new Notificaciones());

    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if(lastPosition != position && getCurrentFragment() != object) {
            lastPosition = position;
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public void destroyItem(@NonNull View container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.remove(position);
    }



    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
