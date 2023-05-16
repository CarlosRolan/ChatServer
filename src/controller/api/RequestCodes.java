package controller.api;

public interface RequestCodes {
    final String PRESENT = "PRESENTATION";
    final String SHOW_ALL_ONLINE = "SHOW_ALL_ONLINE";
    final String SHOW_ALL_CHATS = "SHOW_ALL_CHATS";

    // Single conversation
    final String SINGLE_REQUESTED = "SINGLE_CHAT_REQUESTED";
    final String START_SINGLE = "START_SINGLE";
    final String SEND_DIRECT_MSG = "SEND_DIRECT_MSG";

    // Chat codes
    final String NEW_CHAT = "NEW_CHAT";
    final String CHAT_REGISTERED = "CHAT_REGISTERED";
    final String START_CHAT = "START_CHAT";
    final String TO_CHAT = "TO_CHAT";
    final String FROM_CHAT = "FROM_CHAT";
    final String ADD_MEMBER = "ADD_MEMBER";
    final String UPDATE_CHAT = "UPDATE_CHAT";
    final String SEND_MSG_TO_CHAT = "SEND_MSG_TO_CHAT";
    final String SHOW_MEMBERS = "SHOW_MEMBERS";
    final String GET_CHAT = "GET_CHAT";

    // Client comfirmation
    final String ALLOW = "ALLOW";
    final String DENY = "DENY";

    // Modifiers
    final String BY_ID = "BY_ID";
    final String BY_NICK = "BY_NICK";

    final String INFO_PRESENTATION_START = "PRESENTED to the server as ";
    final String INFO_NO_SERVER_RESPONSE = "NO server response";
    final String INFO_WAITING_RESPONSE = "Waiting to server response";

    final String INFO_CONECXION_REJECTED = "CONECXION_REJECTED";
    final String INFO_COMFIRMATION_SUCCESS = "OK";
    final String INFO_CONECXION_ACCEPTED = "CONECXION_ACCEPTED";

    final String ERROR_PRESENTATION = "Could not present ";
    final String ERROR_SERVER_CONNECTION = "Could not read response from server";

}
