package com.example.todolist;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    @FXML
    private ToggleButton filterToggleButton;
    @FXML
    private Button addBtn;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ListView<TodoItem> todoListView;
    private List<TodoItem> todoItems;
    @FXML
    private TextArea itemsDetailsTextArea;
    @FXML
    private ContextMenu listContextMenu;
    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodayItems;

    public void initialize() {
        /*TodoItem item1 = new TodoItem("Mail birthday card", "Buy a 30th birthday card for John", LocalDate.of(2016, Month.APRIL, 25));
        TodoItem item2 = new TodoItem("Doctor's Appointment", "See Dr. Smith at District 12", LocalDate.of(2016, Month.MAY, 12));
        TodoItem item3 = new TodoItem("Finish design proposal", "Submit to Canvas", LocalDate.of(2016, Month.JUNE, 1));
        TodoItem item4 = new TodoItem("Pickup Doug at the train station", "Arriving on 3:00'clock", LocalDate.of(2016, Month.FEBRUARY, 3));
        TodoItem item5 = new TodoItem("Pick up dry cleaning", "The clothes should be ready for tomorrow", LocalDate.of(2016, Month.DECEMBER, 31));

        todoItems = new ArrayList<TodoItem>();
        todoItems.add(item1);
        todoItems.add(item2);
        todoItems.add(item3);
        todoItems.add(item4);
        todoItems.add(item5);

        TodoData.getInstance().setTodoItems(todoItems);*/

        // Add tooltip for addBtn



        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem todoItem, TodoItem t1) {
                if(t1 != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemsDetailsTextArea.setText(item.getDetails());

                    // Date formatter
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

/*
        todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
*/
        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };
        wantTodayItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return (item.getDeadline().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems());
        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, new Comparator<TodoItem>(){
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });


        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // set Selection item in ListView

        todoListView.getSelectionModel().selectFirst();

        // Cell Factory
        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().equals(LocalDate.now())) {
                                setTextFill(Color.RED);
                            } else if(item.getDeadline().equals(LocalDate.now().plusDays(1))) { // deadline tomorrow
                                setTextFill(Color.BROWN);
                            } else if(item.getDeadline().isBefore(LocalDate.now().minusDays(1))) {
                                setTextFill(Color.GREEN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpy) -> {
                            if(isNowEmpy) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }

    // Set new scene to todoItemDialog.fxml
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this dialog to create a new todo item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
 /*           Parent root = FXMLLoader.load(getClass().getResource("todoItemDialog.fxml"));*/
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        // Add button
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {

            // get the controller of Dialog to call the function processResults
            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResults();
/*
            todoListView.getItems().setAll(TodoData.getInstance().getTodoItems()); // update to the main screen
*/
            todoListView.getSelectionModel().select(newItem);

            System.out.println("Ok pressed");
        } else {
            System.out.println("Cancel pressed");
        }
    }


    public void handleClickListView() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        System.out.println("The selected item is " + item);
        itemsDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
     /*   StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n\n\n");
        sb.append("Due: ");
        sb.append(item.getDeadline().toString());
        itemsDetailsTextArea.setText(sb.toString());
*/
    }

    public void deleteItem(TodoItem item) {
        // create confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to Back Out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)) {
            TodoData.getInstance().deleteTodoItem(item);
        }

    }

    // handle Keypress to delete
    public void handleKeyPressed(KeyEvent keyEvent) {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            if(keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    // Filtering the item has the deadline is today for the toggle Button
    public void handleFilterButton() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodayItems);
            if(filteredList.isEmpty()) {
                itemsDetailsTextArea.clear();
                deadlineLabel.setText("");
            } else if(filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }

    public void handleExit() {
        Platform.exit();
    }
}