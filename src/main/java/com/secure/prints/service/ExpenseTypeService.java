package com.secure.prints.service;

import com.secure.prints.model.ExpenseCategory;
import com.secure.prints.model.ExpenseCode;
import com.secure.prints.model.ExpenseSubcategory;
import com.secure.prints.model.ExpenseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ExpenseTypeService {

    private static List<ExpenseType> expenseTypeList;

    /**
     * Generate Expense Type list for categories and subcategories
     * @return List of ExpenseType
     */
    public static List<ExpenseType> generateExpenseTypeList() {
        ExpenseCategory expenseCategory1 = new ExpenseCategory(400, "Operating Expenses");
        ExpenseCategory expenseCategory2 = new ExpenseCategory(500, "Non-Operating Expenses");
        ExpenseCategory expenseCategory3 = new ExpenseCategory(600, "Selling and Administrative Expenses");
        ExpenseCategory expenseCategory4 = new ExpenseCategory(700, "Vehicle and Transportation Expenses");
        ExpenseCategory expenseCategory5 = new ExpenseCategory(800, "Financial Expenses");
        ExpenseCategory expenseCategory6 = new ExpenseCategory(900, "Miscellaneous");

        List<ExpenseSubcategory> expenseSubcategoryList1 = List.of(
                new ExpenseSubcategory(401, "Rent or Lease", "Office, equipment, or vehicle rentals"),
                new ExpenseSubcategory(402, "Utilities", "Electricity, water, gas, internet, phone"),
                new ExpenseSubcategory(403, "Salaries and Wages", "Employee compensation"),
                new ExpenseSubcategory(404, "Employee Benefits", "Health insurance, retirement contributions"),
                new ExpenseSubcategory(405, "Payroll Taxes", "Employer-paid taxes like Social Security, Medicare"),
                new ExpenseSubcategory(406, "Office Supplies", "Paper, pens, printer ink, etc"),
                new ExpenseSubcategory(407, "Postage and Shipping", "Mailing or courier services"),
                new ExpenseSubcategory(408, "Maintenance and Repairs", "For office or equipment upkeep"),
                new ExpenseSubcategory(409, "Professional Services", "Legal, accounting, consulting fees")
        );

        List<ExpenseSubcategory> expenseSubcategoryList2 = List.of(
                new ExpenseSubcategory(501, "Interest Expense", "Cost of borrowing money (loans, credit lines, bonds)"),
                new ExpenseSubcategory(502, "Income Tax Expense", "Taxes paid on business profits"),
                new ExpenseSubcategory(503, "Loss on Sale of Assets", "When a business sells an asset for less than its book value"),
                new ExpenseSubcategory(504, "Restructuring Costs", "Expenses from reorganizing the business (layoffs, plant closures, etc.)"),
                new ExpenseSubcategory(505, "Write-offs of Uncollectible Debts (Bad Debt Expense)", "When a customer invoice is deemed non-recoverable")
        );

        List<ExpenseSubcategory> expenseSubcategoryList3 = List.of(
                new ExpenseSubcategory(601, "Advertising and Marketing", "Online ads, billboards, flyers"),
                new ExpenseSubcategory(602, "Travel and Meals", "Business travel, meals with clients"),
                new ExpenseSubcategory(603, "Insurance", "Liability, property, workers’ compensation"),
                new ExpenseSubcategory(604, "Bank Fees and Credit Card Processing", "Merchant or transaction fees"),
                new ExpenseSubcategory(605, "Software Subscriptions", "SaaS tools, CRMs, accounting software"),
                new ExpenseSubcategory(606, "Licenses and Permits", "Required by local or federal laws"),
                new ExpenseSubcategory(607, "Training and Education", "Courses, seminars, certifications"),
                new ExpenseSubcategory(608, "Dues and Subscriptions", "Industry memberships, publications")
        );

        List<ExpenseSubcategory> expenseSubcategoryList4 = List.of(
                new ExpenseSubcategory(701, "Fuel and Mileage", ""),
                new ExpenseSubcategory(702, "Vehicle Maintenance and Repairs", ""),
                new ExpenseSubcategory(703, "Vehicle Lease or Depreciation", ""),
                new ExpenseSubcategory(704, "Parking and Tolls", "")
        );

        List<ExpenseSubcategory> expenseSubcategoryList5 = List.of(
                new ExpenseSubcategory(801, "Interest Expense", "On loans or credit cards"),
                new ExpenseSubcategory(802, "Bad Debts", "Uncollectible customer invoices"),
                new ExpenseSubcategory(803, "Depreciation", "Tangible assets (physical items: Buildings, machinery, vehicles, computers)"),
                new ExpenseSubcategory(804, "Amortization", "Intangible assets (non-physical items: Patents, copyrights, trademarks, software licenses, goodwill)")
        );

        List<ExpenseSubcategory> expenseSubcategoryList6 = List.of(
                new ExpenseSubcategory(901, "Gifts", "Within allowable business limits"),
                new ExpenseSubcategory(902, "Charitable Contributions", "If tax-deductible"),
                new ExpenseSubcategory(903, "Penalties and Fines", "Not usually tax-deductible, but recorded")
        );

        ExpenseType expenseType1 = new ExpenseType(expenseCategory1, expenseSubcategoryList1);
        ExpenseType expenseType2 = new ExpenseType(expenseCategory2, expenseSubcategoryList2);
        ExpenseType expenseType3 = new ExpenseType(expenseCategory3, expenseSubcategoryList3);
        ExpenseType expenseType4 = new ExpenseType(expenseCategory4, expenseSubcategoryList4);
        ExpenseType expenseType5 = new ExpenseType(expenseCategory5, expenseSubcategoryList5);
        ExpenseType expenseType6 = new ExpenseType(expenseCategory6, expenseSubcategoryList6);

        expenseTypeList = new ArrayList<>();
        expenseTypeList.add(expenseType1);
        expenseTypeList.add(expenseType2);
        expenseTypeList.add(expenseType3);
        expenseTypeList.add(expenseType4);
        expenseTypeList.add(expenseType5);
        expenseTypeList.add(expenseType6);

        return expenseTypeList;
    }

    /**
     * Search Expense Type list for a keyword in category and subcategory
     * @param keyword keyword
     * @return Filtered list for keyword
     */
    public static List<ExpenseType> searchExpenseTypeList(String keyword) {
        List<ExpenseType> resultList = expenseTypeList;
        String lowerKeyword = keyword.toLowerCase();
        return resultList.stream()
                .map(data -> {
                    boolean categoryMatches = data.getExpenseCategory().getCategoryName().toLowerCase().contains(lowerKeyword);
                    List<ExpenseSubcategory> matchingSubcategories = data.getExpenseSubcategories().stream()
                            .filter(sub -> sub.getSubcategoryName().toLowerCase().contains(lowerKeyword)
                                    || sub.getSubcategoryDescription().toLowerCase().contains(lowerKeyword))
                            .toList();
                    if (categoryMatches || !matchingSubcategories.isEmpty()) {
                        return new ExpenseType(data.getExpenseCategory(), matchingSubcategories);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns expense category and subcategory codes
     * @param subcategoryName subcategoryName
     * @return Optional of codes value
     */
    public static ExpenseCode getExpenseCode(String subcategoryName) {
        Optional<ExpenseCode> optionalResult = expenseTypeList.stream()
                .flatMap(data -> data.getExpenseSubcategories().stream()
                        .filter(sub -> sub.getSubcategoryName().equalsIgnoreCase(subcategoryName))
                        .map(sub -> new ExpenseCode(
                                data.getExpenseCategory().getCategoryCode(),
                                sub.getSubcategoryCode())
                        )
                )
                .findFirst();
        return optionalResult.orElse(null);
    }

}
