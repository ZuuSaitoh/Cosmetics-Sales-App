package com.example.demo.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(99999, "Uncategorized error"),
    INVALID_KEY(1001, "Uncategorized error"),
    USER_EXISTED(1002, "Users existed"),
    USERNAME_INVALID(1003, "Username must be at least 3 characters"),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters"),
    USER_NOT_EXISTED(1005, "Users not existed"),
    UNAUTHENTICATED(1006, "Unauthenticated"),
    INVALID_EMAIL(1007, "Invalid email"),
    EMAIL_EXISTED(1008, "Email existed"),
    ENTER_ALL_FIELDS(1009, "There is a blank field! You should enter all field"),
    PASSWORD_NOT_MATCH(1010, "Password didn't match! Try again!"),
    PHONE_NUMBER_INVALID(1011, "Phone number invalid, please try again"),
    USER_DELETED(1012, "Users deleted"),
    EMAIL_NOT_EXISTED(1013, "Email don't existed!"),
    LOGIN_GG_NOT_PASSWORD(1014, "Can't change the password because you've login by GG method!"),
    INVALID_SERVICE_TYPE(1015, "Service must be Cleaning Pond Service or Maintenance!"),
    SERVICE_NOT_EXISTED(1016, "Service does not existed!"),
    INVALID_STAFF_TYPE(1017,"Staff does not existed!"),
    STAFF_NOT_EXISTED(1018, "Staff not existed"),
    ORDER_NOT_EXISTED(1019, "Order not existed"),
    RATING_MIN(1020, "Rating at least 1"),
    RATING_MAX(1021, "Rating at most 5"),
    SHORT_FEEDBACK(1022, "Feedback at least 5 characters!"),
    CONTRACT_NOT_EXISTED(1023, "Contract is not existed!"),
    DESIGN_NOT_EXISTED(1024,"Design is not existed!"),
    STATUS_NOT_EXISTED(1025,"Status is not existed!"),
    THREE_TIME_UPDATE(1026,"You can't update more than 3 times!"),
    COMPLETE_TRUE(1027,"You can't change the status because this status has set to done!"),
    CONSULTING_STAFF_NOT_EXISTED(1028, "Consulting staff not existed!"),
    DESIGN_STAFF_NOT_EXISTED(1029, "Design staff not existed!"),
    CONSTRUCTION_STAFF_NOT_EXISTED(1030, "Construction staff not existed!"),
    ACCEPTANCE_TEST_NOT_EXISTED(1031, "Acceptance test is not existed!"),
    TRANSACTION_NOT_EXISTED(1032,"Transaction is not existed!"),
    BOOKING_SERVICE_NOT_EXISTED(1033, "Booking service is not existed!"),
    TRANSACTION_DONE(1034,"Transaction has been pay!"),
    SERVICE_TRANSACTION_DONE(1035,"Transaction has been pay!"),
    SERVICE_TRANSACTION_NOT_EXISTED(1036,"Transaction not existed!"),
    DISCOUNT_NOT_EXISTED(1037, "Discount not existed!"),
    FORM_NOT_EXISTED(1038, "Form not existed!"),
    ORDER_CREATED(1038, "Order have been created with this form!"),
    ORDER_COMPLETE(1039,"The order is done so that you can't set end date!"),
    MAX_POINT(1040,"You are using points more than the number that you have!"),
    INVALID_ROLE(1041, "The role is not invalid"),
    CATEGORY_EXISTED(1042,"The category already existed!"),
    CATEGORY_NOT_EXISTED(1043,"The category is not existed!"),
    PRODUCT_NOT_EXISTED(1044,"Product is not existed!"),
    PRODUCT_EXISTED(1045,"Product existed!"),
    QUANTITY_NOT_CHANGED(1046,"Quantity not changed!"),
    QUANTITY_CANNOT_BE_NEGATIVE(1047,"You can't input negative quantity!"),
    PRICE_MUST_BE_POSITIVE(1048,"Price must be positive!"),
    PRICE_TOO_HIGH(1049,"Price can't higher than 1 billions"),
    CATEGORY_ID_CANNOT_BE_NEGATIVE(1050,"Category ID can't be lower than 1!"),
    ACTIVE_CART_NOT_EXISTED(1051,"User has no active cart"),
    ACTIVE_CART_EXISTED(1052,"User already have a active cart"),
    CART_NOT_EXISTED(1053, "Cart is not existed!"),
    CART_HAVE_NOTHING(1054,"Cart don't have any item!"),
    PRODUCT_QUANTITY_NOT_FULLFILL(1055,"You add product's quantity bigger than instock quantity!"),
    PRODUCT_ALREADY_IN_CART(1056, "You have already add this product to cart!"),
    PRODUCT_NOT_IN_CART(1057,"The product is not existed in your cart"),
    LOCATION_NOT_EXISTED(1058,"Store location is not existed!"),
    CART_ITEM_NOT_EXISTED(1059,"Cart item is not existed!"),
    ORDER_EXISTED(1061,"Order existed!"),
    INVALID_ORDER_STATUS(1062,"Order status is not valid!"),
    PAYMENT_METHOD_NOT_VALID(1063,"Payment method is not valid!"),
    BILLING_ADDRESS_NOT_VALID(1064,"Billing address is not valid!"),
    OUT_OF_STOCK(1065,"Out of stock!"),
    PAYMENT_METHOD_NOT_SUPPORTED(1066,"Payment method not supported! Only support COD, Momo, ZaloPay"),
    CART_ALREADY_CHECKED_OUT(1067,"Cart already checked out!"),
    STATUS_NOT_VALID(1068,"Status is not valid! Order's status must be \"Processing\", \"Shipped\", \"Delivered\", \"Cancelled\""),;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
