package com.example.todolist;

import static java.util.Calendar.MINUTE;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ModelData> arrayList;
    private static final String TAG = "MainActivity";
    private DatabaseHelper databaseHelper;

    public ItemAdapter(Context context, ArrayList<ModelData> arrayList) {
        super();
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return this.arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Memasukkan data ke database
    private void updateDataToDb(ModelData model) {
        databaseHelper = new DatabaseHelper(context);
        boolean updateData = databaseHelper.updateData(model);
        if (updateData) {
            try {
                populateListView();
                toastMsg("Tugas diperbarui");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            toastMsg("Terjadi kesalahan saat menyimpan!");
    }

    //Mengambil seluruh data dari database ke listview
    private void populateListView() {
        try {
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        convertView = layoutInflater.inflate(R.layout.daftar_todo, null);
        TextView titleTextView = convertView.findViewById(R.id.title);
        TextView dateTextView = convertView.findViewById(R.id.dateTitle);
        TextView timeTextView = convertView.findViewById(R.id.timeTitle);

        ModelData modelData = arrayList.get(position);
        titleTextView.setText(modelData.getTitle());
        dateTextView.setText(modelData.getDate());
        timeTextView.setText(modelData.getTime());

        final ImageView delImageView = convertView.findViewById(R.id.delete);
        final ImageView updImageView = convertView.findViewById(R.id.update);
        delImageView.setTag(position);
        updImageView.setTag(position);



        //Menghapus tugas dari database saat icon hapus di klik
        delImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = (int) v.getTag();
                Log.d(ItemAdapter.class.getSimpleName(), "Position : " + pos);
                deleteItem(pos);
            }
        });

        //memperbarui tugas dari database saat icon pencil diklik
        updImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int poss = (int) view.getTag();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(layoutInflater.getContext());
                @SuppressLint("InflateParams")
                final View dialogView = layoutInflater.inflate(R.layout.custom_dialog_todo, null);
                dialogBuilder.setView(dialogView);

                final EditText judul = dialogView.findViewById(R.id.edit_title);
                final TextView tanggal = dialogView.findViewById(R.id.date);
                final TextView waktu = dialogView.findViewById(R.id.time);

                judul.setText(titleTextView.getText());

                final long date = System.currentTimeMillis();
                SimpleDateFormat dateSdf = new SimpleDateFormat("d MMMM");
                String dateString = dateSdf.format(date);
                tanggal.setText(dateString);

                SimpleDateFormat timeSdf = new SimpleDateFormat("hh : mm a");
                String timeString = timeSdf.format(date);
                waktu.setText(timeString);

                final Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());

                //Set tanggal
                tanggal.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        final DatePickerDialog datePickerDialog = new DatePickerDialog(layoutInflater.getContext(),
                                new DatePickerDialog.OnDateSetListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String newMonth = getMonth(monthOfYear + 1);
                                        tanggal.setText(dayOfMonth + " " + newMonth);
                                        cal.set(Calendar.YEAR, year);
                                        cal.set(Calendar.MONTH, monthOfYear);
                                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    }
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                        datePickerDialog.getDatePicker().setMinDate(date);
                    }
                });

                //Set waktu
                waktu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(layoutInflater.getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        String time;
                                        @SuppressLint("DefaultLocale") String minTime = String.format("%02d", minute);
                                        if (hourOfDay >= 0 && hourOfDay < 12) {
                                            time = hourOfDay + " : " + minTime + " AM";
                                        } else {
                                            if (hourOfDay != 12) {
                                                hourOfDay = hourOfDay - 12;
                                            }
                                            time = hourOfDay + " : " + minTime + " PM";
                                        }
                                        waktu.setText(time);
                                        cal.set(Calendar.HOUR, hourOfDay);
                                        cal.set(Calendar.MINUTE, minute);
                                        cal.set(Calendar.SECOND, 0);
                                        Log.d(TAG, "onTimeSet: Time has been set successfully");
                                    }
                                }, cal.get(Calendar.HOUR), cal.get(MINUTE), false);
                        timePickerDialog.show();
                    }
                });

                dialogBuilder.setTitle("Buat tugas baru");
                dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String title = judul.getText().toString();
                        String date = tanggal.getText().toString();
                        String time = waktu.getText().toString();
                        if (title.length() != 0) {
                            try {
                                modelData.setTitle(title);
                                modelData.setDate(date);
                                modelData.setTime(time);
                                updateDataToDb(modelData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            toastMsg("Tugas tidak boleh kosong!");
                        }
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();

            }
        });
        return convertView;
    }

    //Menghapus tugas dari listview
    private void deleteItem(int position) {
        deleteItemFromDb(arrayList.get(position).getId());
        arrayList.remove(position);
        notifyDataSetChanged();
    }

    //Menghapus tugas dari database
    private void deleteItemFromDb(int id) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try {
            databaseHelper.deleteData(id);
            toastMsg("Tugas dihapus");
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg("Terjadi kesalahan saat menghapus");
        }
    }

    //Metode pesan toast
    private void toastMsg(String msg) {
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER,0,0);
        t.show();
    }

    //Mengkonversi bulan dari huruf menjadi angka
    private String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month - 1];
    }
}
