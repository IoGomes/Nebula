package Nebula.Android.Nebula_View.Utils;

import android.content.Context;
import android.view.View;

public class NavBar_Inserts {
        public static void adjustPaddingForNavigationBar(View view, Context context) {
            if (view == null || context == null) return;

            int navigationBarHeight = getNavigationBarHeight(context);
            int left = view.getPaddingLeft();
            int top = view.getPaddingTop();
            int right = view.getPaddingRight();
            int bottom = view.getPaddingBottom();

            view.setPadding(left, top, right, bottom + navigationBarHeight);
        }

        private static int getNavigationBarHeight(Context context) {
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return context.getResources().getDimensionPixelSize(resourceId);
            }
            return 0;
        }
    }
