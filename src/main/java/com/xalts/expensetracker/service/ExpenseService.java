package com.xalts.expensetracker.service;

import com.xalts.expensetracker.entity.Expense;
import com.xalts.expensetracker.entity.User;
import com.xalts.expensetracker.repository.ExpenseRepository;
import com.xalts.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public Expense addExpense(Expense expense, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return expenseRepository.findByUser(user);
    }

    public List<Expense> getExpensesByDateRange(LocalDate start, LocalDate end, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return expenseRepository.findByUserAndDateBetween(user, start, end);
    }

    public void deleteExpense(Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!exp.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized delete attempt");
        }

        expenseRepository.deleteById(id);
    }

    public double getTotalExpenseForDateRange(LocalDate start, LocalDate end, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return expenseRepository.findByUserAndDateBetween(user, start, end)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<String, Double> getCategoryWiseBreakdown(LocalDate start, LocalDate end, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return expenseRepository.findByUserAndDateBetween(user, start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public Map<String, Object> getMonthlyReport(int year, int month, Principal principal) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        double total = getTotalExpenseForDateRange(start, end, principal);
        Map<String, Double> breakdown = getCategoryWiseBreakdown(start, end, principal);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", YearMonth.of(year, month).toString());
        report.put("total", total);
        report.put("breakdown", breakdown);

        return report;
    }

}
