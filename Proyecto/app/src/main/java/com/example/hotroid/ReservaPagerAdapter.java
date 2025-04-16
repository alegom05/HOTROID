package com.example.hotroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReservaPagerAdapter extends FragmentStateAdapter {

    public ReservaPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new FragmentReservaActivos();
            case 1: return new FragmentReservaPasados();
            case 2: return new FragmentReservaCancelados();
            default: return new FragmentReservaActivos();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
