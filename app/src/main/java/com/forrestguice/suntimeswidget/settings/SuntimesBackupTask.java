/**
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.settings;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWidgetListActivity;
import com.forrestguice.suntimeswidget.tiles.ClockTileService;
import com.forrestguice.suntimeswidget.tiles.NextEventTileService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Backup all Suntimes settings to json backup file.
 * Backup contents may include: AppSettings, WidgetSettings, AlarmClockItems
 */
public class SuntimesBackupTask extends WidgetSettingsExportTask
{
    public static final String KEY_APPSETTINGS = "AppSettings";
    public static final String KEY_WIDGETSETTINGS = "WidgetSettings";
    public static final String KEY_ALARMITEMS = "AlarmItems";
    public static final String KEY_EVENTITEMS = "EventItems";
    public static final String KEY_PLACEITEMS = "PlaceItems";

    public static final String[] ALL_KEYS = new String[] {
            KEY_APPSETTINGS, KEY_WIDGETSETTINGS
            //, KEY_ALARMITEMS    // TODO: implement
            //, KEY_EVENTITEMS    // TODO: implement
            //, KEY_PLACEITEMS    // TODO: implement
    };

    public static final String DEF_EXPORT_TARGET = "SuntimesBackup";

    public SuntimesBackupTask(Context context, String exportTarget) {
        super(context, exportTarget);
    }
    public SuntimesBackupTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache) {
        super(context, exportTarget, useExternalStorage, saveToCache);
    }
    public SuntimesBackupTask(Context context, Uri uri) {
        super(context, uri);
    }

    /**
     * @param key KEY_APPSETTINGS, KEY_WIDGETSETTINGS, KEY_ALARMITEMS
     * @param value true, false
     */
    public void includeInBackup(String key, boolean value) {
        includedKeys.put(key, value);
    }
    public void includeInBackup(String... keys) {
        for (String key : keys) {
            includeInBackup(key, true);
        }
    }
    public void includeInBackup(String[] keys, boolean[] include) {
        for (int i=0; i<keys.length; i++) {
            includeInBackup(keys[i], (i<include.length && include[i]));
        }
    }
    public void includeAll() {
        includeInBackup(ALL_KEYS);
    }
    protected Map<String,Boolean> includedKeys = new HashMap<>();

    /**
     * writes
     *   {
     *     "AppSettings": { ContentValues }
     *     "WidgetSettings": [{ ContentValues }, ...]
     *     "AlarmItems": [{ AlarmClockItem }, ...]
     *   }
     */
    @Override
    public boolean export( Context context, BufferedOutputStream out ) throws IOException
    {
        out.write("{".getBytes());
        int c = 0;    // keys written

        if (includedKeys.containsKey(KEY_APPSETTINGS) && includedKeys.get(KEY_APPSETTINGS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_APPSETTINGS + "\": ").getBytes());    // include AppSettings
            writeAppSettingsJSONObject(context, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_WIDGETSETTINGS) && includedKeys.get(KEY_WIDGETSETTINGS) && appWidgetIds.size() > 0)
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_WIDGETSETTINGS + "\": ").getBytes());    // include WidgetSettings
            writeWidgetSettingsJSONArray(context, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_ALARMITEMS) && includedKeys.get(KEY_ALARMITEMS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_ALARMITEMS + "\": ").getBytes());    // include AlarmItems
            writeAlarmItemsJSONArray(context, out);
            c++;
        }

        out.write("}".getBytes());
        out.flush();
        return true;
    }

    /**
     * writes
     *   { ContentValues }
     */
    protected void writeAppSettingsJSONObject(Context context, BufferedOutputStream out) throws IOException
    {
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(context);         // AppSettings are stored in default shared prefs
        String json = WidgetSettingsImportTask.ContentValuesJson.toJson(toContentValues(appPrefs));
        out.write(json.getBytes());
        out.flush();
    }

    /**
     * writes
     *   [{ AlarmClockItem}, ...]
     */
    protected void writeAlarmItemsJSONArray(Context context, BufferedOutputStream out) throws IOException
    {
        out.write("{}".getBytes());  // TODO: write alarm items
        out.flush();
    }

    public static void exportSettings(final Context context, @Nullable final Uri uri, final ExportTask.TaskListener exportListener)
    {
        Log.i("ExportSettings", "Starting export task: " + uri);
        SuntimesBackupTask.chooseBackupContent(context, SuntimesBackupTask.ALL_KEYS, false, new SuntimesBackupTask.ChooseBackupDialogListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked)
            {
                WidgetSettingsExportTask.addWidgetMetadata(context);
                SuntimesBackupTask task = (uri != null ? new SuntimesBackupTask(context, uri)
                        : new SuntimesBackupTask(context, SuntimesBackupTask.DEF_EXPORT_TARGET, true, true));  // export to external cache;
                task.setTaskListener(exportListener);
                task.includeInBackup(keys, checked);
                task.setAppWidgetIds(getAllWidgetIds(context));
                task.execute();
            }
        });
    }

    protected static ArrayList<Integer> getAllWidgetIds(Context context)
    {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Class widgetClass : SuntimesWidgetListActivity.WidgetListAdapter.ALL_WIDGETS) {
            ids.addAll(getAllWidgetIds(context, widgetClass));
        }
        ids.add(0);                                                    // include app config and quick settings tiles
        ids.add(ClockTileService.CLOCKTILE_APPWIDGET_ID);
        ids.add(NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID);
        return ids;
    }
    protected static ArrayList<Integer> getAllWidgetIds(Context context, Class widgetClass)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        String packageName = context.getPackageName();
        ArrayList<Integer> ids = new ArrayList<>();
        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(packageName, widgetClass.getName()));
        for (int id : widgetIds) {
            ids.add(id);
        }
        return ids;
    }

    /**
     * displayStringForBackupKey
     * @param context Context
     * @param key backupKey, e.g. KEY_APPSETTINGS
     * @return display string (or the key itself if unrecognized)
     */
    public static CharSequence displayStringForBackupKey(Context context, String key)
    {
        if (SuntimesBackupTask.KEY_APPSETTINGS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_appsettings));
        }
        if (SuntimesBackupTask.KEY_WIDGETSETTINGS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_widgetsettings));
        }
        if (SuntimesBackupTask.KEY_ALARMITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_alarmitems));
        }
        if (SuntimesBackupTask.KEY_EVENTITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_eventitems));
        }
        if (SuntimesBackupTask.KEY_PLACEITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_placeitems));
        }
        return key;
    }

    /**
     * ChooseBackupDialogListener
     */
    public interface ChooseBackupDialogListener {
        void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked);
    }

    /**
     * chooseBackupContent
     * @param context Context
     * @param keys key to choose from
     * @param isImport true importing content, false exporting content
     * @param onClickListener dialog listener
     */
    public static void chooseBackupContent(final Context context, Set<String> keys, boolean isImport, @NonNull final ChooseBackupDialogListener onClickListener) {
        chooseBackupContent(context, keys.toArray(new String[0]), isImport, onClickListener);
    }
    public static void chooseBackupContent(final Context context, final String[] keys, boolean isImport, @NonNull final ChooseBackupDialogListener onClickListener)
    {
        final CharSequence[] items = new CharSequence[keys.length];
        final boolean[] checked = new boolean[keys.length];
        for (int i=0; i<items.length; i++) {
            items[i] = SuntimesBackupTask.displayStringForBackupKey(context, keys[i]);
            checked[i] = true;
        }

        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(isImport ? R.string.configAction_restoreBackup : R.string.configAction_createBackup))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                })
                .setPositiveButton(context.getString(isImport ? R.string.configAction_import : R.string.configAction_export), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        int p = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        onClickListener.onClick(dialog, p, keys, checked);
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }

    /**
     * showIOResultSnackbar
     */
    public static void showIOResultSnackbar(final Context context, final View view, boolean result, final CharSequence message, @Nullable final CharSequence report)
    {
        if (context != null && view != null)
        {
            Snackbar snackbar = Snackbar.make(view, message, (result ? 7000 : Snackbar.LENGTH_LONG));
            if (report != null)
            {
                snackbar.setAction(context.getString(R.string.configAction_info), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setTitle(message)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setMessage(report);
                        dialog.show();
                    }
                });
            }
            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.show();
        }
    }

}
