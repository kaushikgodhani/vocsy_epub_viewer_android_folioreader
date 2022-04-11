package com.folioreader.ui.adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.folioreader.ui.fragment.FolioPageFragment;
import com.folioreader.viewmodels.PageTrackerViewModel;

import org.readium.r2.shared.Link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter {

    private static final String LOG_TAG = FolioPageFragmentAdapter.class.getSimpleName();
    private List<Link> mSpineReferences;
    private String mEpubFileName;
    private String mBookId;
    private ArrayList<FolioPageFragment> fragments;
    private ArrayList<FolioPageFragment.SavedState> savedStateList;
    private PageTrackerViewModel viewModel;

    public FolioPageFragmentAdapter(FragmentManager fragmentManager, List<Link> spineReferences,
                                    String epubFileName, String bookId, PageTrackerViewModel viewModel) {
        super(fragmentManager);
        this.mSpineReferences = spineReferences;
        this.mEpubFileName = epubFileName;
        this.mBookId = bookId;
        fragments = new ArrayList<>(Arrays.asList(new FolioPageFragment[mSpineReferences.size()]));
        this.viewModel = viewModel;

        for (Link spineReference:spineReferences) {
            Log.d("loop", "FolioPageFragmentAdapter: " + spineReference.toJSON());
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.set(position, null);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        FolioPageFragment fragment = (FolioPageFragment) super.instantiateItem(container, position);
        fragments.set(position, fragment);
        return fragment;
    }

    @Override
    public FolioPageFragment getItem(int position) {

        if (mSpineReferences.size() == 0 || position < 0 || position >= mSpineReferences.size())
            return null;

        FolioPageFragment fragment = fragments.get(position);
        if (fragment == null) {
            fragment = FolioPageFragment.newInstance(position,
                    mEpubFileName,
                    mSpineReferences.get(position),
                    mBookId,
                    viewModel);
            fragments.set(position, fragment);
        }
        return fragment;
    }

    public ArrayList<FolioPageFragment> getFragments() {
        return fragments;
    }

    public ArrayList<FolioPageFragment.SavedState> getSavedStateList() {

        if (savedStateList == null) {
            try {
                Field field = FragmentStatePagerAdapter.class.getDeclaredField("mSavedState");
                field.setAccessible(true);
                savedStateList = (ArrayList<FolioPageFragment.SavedState>) field.get(this);
            } catch (Exception e) {
                Log.e(LOG_TAG, "-> ", e);
            }
        }

        return savedStateList;
    }

    public static Bundle getBundleFromSavedState(Fragment.SavedState savedState) {

        Bundle bundle = null;
        try {
            Field field = Fragment.SavedState.class.getDeclaredField("mState");
            field.setAccessible(true);
            bundle = (Bundle) field.get(savedState);
        } catch (Exception e) {
            Log.v(LOG_TAG, "-> " + e);
        }
        return bundle;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }
}