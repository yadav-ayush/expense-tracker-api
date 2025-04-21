package com.xalts.expensetracker.controller;

import com.xalts.expensetracker.entity.Expense;
import com.xalts.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense, Principal principal) {
        return ResponseEntity.ok(expenseService.addExpense(expense, principal));
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAll(Principal principal) {
        return ResponseEntity.ok(expenseService.getAllExpenses(principal));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Expense>> getByDate(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Principal principal
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(start, end, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        expenseService.deleteExpense(id, principal);
        return ResponseEntity.ok("Deleted successfully");
    }
}
