package neoflex.chulkov.dto.enums;

public enum Theme {
    FINISH_REGISTRATION("Завершение регистрации"),
    CREATE_DOCUMENT("Создание документов"),
    SEND_DOCUMENTS("Отправка документов"),
    SEND_SES("Отправка ПЕП"),
    CREDIT_ISSUED("Проблема с кредитом"),
    STATEMENT_DENIED("Заявление отклонено");

    String title;
    Theme(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
