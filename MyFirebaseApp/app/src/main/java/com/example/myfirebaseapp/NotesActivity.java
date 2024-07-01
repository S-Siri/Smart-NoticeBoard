package com.example.myfirebaseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etNote;
    private ListView lvNotes;

    private Button record_note;
    private ArrayList<String> notesList;
    private ArrayList<Integer> notesIdList;
    private ArrayAdapter<String> adapter;
    private int selectedNoteId = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        dbHelper = new DatabaseHelper(this);
        etNote = findViewById(R.id.etNote);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        lvNotes = findViewById(R.id.lvNotes);
        record_note=findViewById(R.id.button2);

        notesList = new ArrayList<>();
        notesIdList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notesList);
        lvNotes.setAdapter(adapter);

        loadNotes();

        record_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotesActivity.this, VoiceActivity.class));
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = etNote.getText().toString();
                if (!note.isEmpty()) {
                    if (dbHelper.addNote(note)) {
                        etNote.setText("");
                        Toast.makeText(NotesActivity.this, "Note added", Toast.LENGTH_SHORT).show();
                        loadNotes();
                    } else {
                        Toast.makeText(NotesActivity.this, "Error adding note", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = etNote.getText().toString();
                if (!note.isEmpty() && selectedNoteId != -1) {
                    if (dbHelper.updateNoteById(selectedNoteId, note)) {
                        etNote.setText("");
                        btnUpdate.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.VISIBLE);
                        selectedNoteId = -1;
                        Toast.makeText(NotesActivity.this, "Note updated", Toast.LENGTH_SHORT).show();
                        loadNotes();
                    } else {
                        Toast.makeText(NotesActivity.this, "Error updating note", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        lvNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int noteId = notesIdList.get(position);
                if (dbHelper.deleteNoteById(noteId)) {
                    Toast.makeText(NotesActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                    loadNotes();
                } else {
                    Toast.makeText(NotesActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedNoteId = notesIdList.get(position);
                etNote.setText(notesList.get(position));
                btnUpdate.setVisibility(View.VISIBLE);
                btnAdd.setVisibility(View.GONE);
            }
        });
    }

    @SuppressLint("Range")
    private void loadNotes() {
        notesList.clear();
        notesIdList.clear();
        Cursor cursor = dbHelper.getAllNotes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                notesIdList.add(cursor.getInt(cursor.getColumnIndex("id")));
                notesList.add(cursor.getString(cursor.getColumnIndex("note")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
