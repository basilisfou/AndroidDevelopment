package com.example.vasilis.TheGadgetFlow;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Vasilis Fouroulis on 30/12/2015
 */
public class OnBoarding extends AppCompatActivity {

//    private static final String TAG ="OnBoarding";
//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;
//    private String cookie;
//    private ViewPager mViewPager;
//    private CirclePageIndicator mIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.edit_profile);
//        sharedPreferences = this.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
//        editor = sharedPreferences.edit();
//        cookie = sharedPreferences.getString(Constants.COOKIE, cookie);
//
//        /** already sign in */
//        if(cookie != null) {
//            Intent intent = new Intent(this, MainActivity.class);
//            editor.putBoolean(Constants.LOG_IN_TO_MAIN,true);
//            editor.apply();
//            startActivity(intent);
//            this.finish();
//        }
//
//        mViewPager = (ViewPager) findViewById(R.id.viewPager);
//        mViewPager.setAdapter(new MyPagerAdapter(getFragmentManager()));
    }

//    private class MyPagerAdapter extends Fragment {

//        public MyPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int pos) {
//            switch(pos) {
//
//                case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
//                case 1: return SecondFragment.newInstance("SecondFragment, Instance 1");
//                case 2: return ThirdFragment.newInstance("ThirdFragment, Instance 1");
//                case 3: return ThirdFragment.newInstance("ThirdFragment, Instance 2");
//                case 4: return ThirdFragment.newInstance("ThirdFragment, Instance 3");
//                default: return ThirdFragment.newInstance("ThirdFragment, Default");
//            }
//        }
//
//        @Override
//        public int getCount() {
//            return 5;
//        }
//    }
}