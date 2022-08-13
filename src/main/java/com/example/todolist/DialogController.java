package com.example.todolist;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {
    @FXML
    private TextField shortDescription;
    @FXML
    private TextArea detailArea;
    @FXML
    private DatePicker deadlinePicker;

    public TodoItem processResults() {
        String shortDes = shortDescription.getText().trim();
        String details = detailArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        TodoItem newItem = new TodoItem(shortDes, details, deadlineValue);
        TodoData.getInstance().addTodoItem(newItem); // add to singleton
        return newItem;
    }
}
