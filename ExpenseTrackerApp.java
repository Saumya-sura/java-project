import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class Expense {
    private String description;
    private double amount;
    private LocalDate date;

    public Expense(String description, double amount, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%-20s %-10.2f %s", description, amount, date);
    }
}

class ExpenseTracker {
    private List<Expense> expenses;
    private double balance;
    private Stack<Action> undoStack;

    public ExpenseTracker() {
        expenses = new ArrayList<>();
        balance = 0;
        undoStack = new Stack<>();
    }

    public void addExpense(String description, double amount, LocalDate date) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty or whitespace.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive and non-zero.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance. Please add money first.");
        }
        Expense exp = new Expense(description, amount, date);
        expenses.add(exp);
        balance -= amount;
        undoStack.push(new Action(ActionType.ADD_EXPENSE, exp, expenses.size() - 1, amount));
        // redoStack.clear();
    }

    public void addMoney(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        balance += amount;
        undoStack.push(new Action(ActionType.ADD_MONEY, null, -1, amount));
        // redoStack.clear();
    }

    public void listExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }
        System.out.println("Index Description          Amount     Date");
        System.out.println("---------------------------------------------");
        for (int i = 0; i < expenses.size(); i++) {
            System.out.printf("%5d %s\n", i + 1, expenses.get(i));
        }
    }

    public double getTotal() {
        double total = 0;
        for (Expense e : expenses) {
            total += e.getAmount();
        }
        return total;
    }

    public double getBalance() {
        return balance;
    }

    public void editExpense(int index, String newDesc, double newAmt, LocalDate newDate) {
        if (index < 0 || index >= expenses.size()) {
            throw new IllegalArgumentException("Invalid expense index.");
        }
        if (newDesc == null || newDesc.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty or whitespace.");
        }
        if (newAmt <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive and non-zero.");
        }
        Expense old = expenses.get(index);
        double diff = newAmt - old.getAmount();
        if (diff > balance) {
            throw new IllegalArgumentException("Insufficient balance for this update.");
        }
        Expense oldCopy = new Expense(old.getDescription(), old.getAmount(), old.getDate());
        old.setDescription(newDesc);
        old.setAmount(newAmt);
        old.setDate(newDate);
        balance -= diff;
        undoStack.push(new Action(ActionType.EDIT_EXPENSE, oldCopy, index, diff));
        // redoStack.clear();
    }

    public void deleteExpense(int index) {
        if (index < 0 || index >= expenses.size()) {
            throw new IllegalArgumentException("Invalid expense index.");
        }
        Expense removed = expenses.remove(index);
        balance += removed.getAmount();
        undoStack.push(new Action(ActionType.DELETE_EXPENSE, removed, index, removed.getAmount()));
        // redoStack.clear();
    }

    public void showSummaryByMonth(int year, int month) {
        double total = 0;
        System.out.println("Expenses for " + year + "-" + String.format("%02d", month) + ":");
        for (Expense e : expenses) {
            LocalDate d = e.getDate();
            if (d.getYear() == year && d.getMonthValue() == month) {
                System.out.println(e);
                total += e.getAmount();
            }
        }
        System.out.printf("Total: %.2f\n", total);
    }

    public void showSummaryByYear(int year) {
        double total = 0;
        System.out.println("Expenses for year " + year + ":");
        for (Expense e : expenses) {
            LocalDate d = e.getDate();
            if (d.getYear() == year) {
                System.out.println(e);
                total += e.getAmount();
            }
        }
        System.out.printf("Total: %.2f\n", total);
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        Action action = undoStack.pop();
        switch (action.type) {
            case ADD_EXPENSE:
                Expense exp = expenses.remove(action.index);
                balance += exp.getAmount();
                break;
            case DELETE_EXPENSE:
                expenses.add(action.index, action.expense);
                balance -= action.amount;
                break;
            case EDIT_EXPENSE:
                Expense curr = expenses.get(action.index);
                double diff = curr.getAmount() - action.expense.getAmount();
                curr.setDescription(action.expense.getDescription());
                curr.setAmount(action.expense.getAmount());
                curr.setDate(action.expense.getDate());
                balance += diff;
                break;
            case ADD_MONEY:
                balance -= action.amount;
                break;
        }
    }



    private enum ActionType { ADD_EXPENSE, DELETE_EXPENSE, EDIT_EXPENSE, ADD_MONEY }
    private static class Action {
        ActionType type;
        Expense expense;
        int index;
        double amount;
        Action(ActionType type, Expense expense, int index, double amount) {
            this.type = type;
            this.expense = expense;
            this.index = index;
            this.amount = amount;
        }
    }
}

public class ExpenseTrackerApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpenseTracker tracker = new ExpenseTracker();
        boolean running = true;
        while (running) {
            System.out.println("\nFinance Manager CLI");
            System.out.println("1. Add Money");
            System.out.println("2. Add Expense");
            System.out.println("3. List Expenses");
            System.out.println("4. Edit Expense");
            System.out.println("5. Delete Expense");
            System.out.println("6. Show Total Expenses");
            System.out.println("7. Show Balance");
            System.out.println("8. Monthly Summary");
            System.out.println("9. Yearly Summary");
            System.out.println("10. Undo");
            // Removed Redo option
            System.out.println("11. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            boolean actionDone = false;
            switch (choice) {
                case "1":
                    try {
                        System.out.print("Enter amount to add: ");
                        double amt = Double.parseDouble(scanner.nextLine());
                        tracker.addMoney(amt);
                        System.out.println("Money added successfully.");
                        actionDone = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "2":
                    try {
                        System.out.print("Enter description: ");
                        String desc = scanner.nextLine();
                        System.out.print("Enter amount: ");
                        double amt = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter date (yyyy-mm-dd): ");
                        String dateStr = scanner.nextLine();
                        LocalDate date = parseDate(dateStr);
                        tracker.addExpense(desc, amt, date);
                        System.out.println("Expense added successfully.");
                        actionDone = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "3":
                    tracker.listExpenses();
                    break;
                case "4":
                    try {
                        tracker.listExpenses();
                        System.out.print("Enter expense index to edit: ");
                        int idx = Integer.parseInt(scanner.nextLine()) - 1;
                        System.out.print("Enter new description: ");
                        String newDesc = scanner.nextLine();
                        System.out.print("Enter new amount: ");
                        double newAmt = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter new date (yyyy-mm-dd): ");
                        String newDateStr = scanner.nextLine();
                        LocalDate newDate = parseDate(newDateStr);
                        tracker.editExpense(idx, newDesc, newAmt, newDate);
                        System.out.println("Expense updated successfully.");
                        actionDone = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter valid numbers.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "5":
                    try {
                        tracker.listExpenses();
                        System.out.print("Enter expense index to delete: ");
                        int idx = Integer.parseInt(scanner.nextLine()) - 1;
                        tracker.deleteExpense(idx);
                        System.out.println("Expense deleted successfully.");
                        actionDone = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "6":
                    System.out.println("Index Description          Amount     Date");
                    System.out.println("---------------------------------------------");
                    tracker.listExpenses();
                    System.out.printf("Total Expenses: %.2f\n", tracker.getTotal());
                    break;
                case "7":
                    System.out.println("Index Description          Amount     Date");
                    System.out.println("---------------------------------------------");
                    tracker.listExpenses();
                    System.out.printf("Current Balance: %.2f\n", tracker.getBalance());
                    break;
                case "8":
                    try {
                        System.out.print("Enter year (yyyy): ");
                        int year = Integer.parseInt(scanner.nextLine());
                        if (year < 1900) {
                            throw new IllegalArgumentException("Year must be 1900 or later.");
                        }
                        System.out.print("Enter month (1-12): ");
                        int month = Integer.parseInt(scanner.nextLine());
                        if (month < 1 || month > 12) {
                            throw new IllegalArgumentException("Month must be between 1 and 12.");
                        }
                        System.out.println("Index Description          Amount     Date");
                        System.out.println("---------------------------------------------");
                        tracker.showSummaryByMonth(year, month);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter valid numbers.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "9":
                    try {
                        System.out.print("Enter year (yyyy): ");
                        int year = Integer.parseInt(scanner.nextLine());
                        if (year < 1900) {
                            throw new IllegalArgumentException("Year must be 1900 or later.");
                        }
                        System.out.println("Index Description          Amount     Date");
                        System.out.println("---------------------------------------------");
                        tracker.showSummaryByYear(year);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid year.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "10":
                    tracker.undo();
                    break;
                // Removed redo option
                case "12":
                    running = false;
                    System.out.println("Exiting Finance Manager. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            // Offer undo/continue after any action that changed state
            if (actionDone) {
                System.out.print("Would you like to Undo (u) or Continue (c)? ");
                String resp = scanner.nextLine().trim().toLowerCase();
                if (resp.equals("u")) {
                    tracker.undo();
                } else if (!resp.equals("c")) {
                    throw new IllegalArgumentException("Invalid input. Please enter only 'u' for Undo or 'c' for Continue.");
                }
            }
        }
        scanner.close();
    }

    private static LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format or value. Please enter date as yyyy-mm-dd and ensure it is a valid date.");
        }
    }
}
