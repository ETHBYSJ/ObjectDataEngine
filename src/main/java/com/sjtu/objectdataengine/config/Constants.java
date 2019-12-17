package com.sjtu.objectdataengine.config;

public class Constants {
    public final static int BLOCK_LENGTH = 3;
    //-----------RESULT CODES-----------------//
    //-----------TREE--------------
    // TREE CREATE
    public final static String TREE_CREATE_SUCCESS = "2000";
    public final static String TREE_CREATE_EMPTY_ID = "2001";
    public final static String TREE_CREATE_EMPTY_NAME = "2002";
    public final static String TREE_CREATE_EMPTY_PARENT = "2003";
    public final static String TREE_CREATE_FAIL = "2004";
    // TREE DELETE
    public final static String TREE_DELETE_SUCCESS = "2100";
    public final static String TREE_DELETE_EMPTY_ID = "2101";
    public final static String TREE_DELETE_NODE_NOT_FOUND = "2102";
    public final static String TREE_DELETE_FAIL = "2103";
    // TREE MODIFY
    public final static String TREE_MODIFY_SUCCESS = "2200";
    public final static String TREE_MODIFY_EMPTY_ID = "2201";
    public final static String TREE_MODIFY_NODE_NOT_FOUND = "2202";
    public final static String TREE_MODIFY_FAIL = "2203";
    // GET TREE
    public final static String TREE_GET_SUCCESS = "2300";
    public final static String TREE_GET_FAIL = "2301";
    // GET NODE
    public final static String NODE_GET_SUCCESS = "2400";
    public final static String NODE_GET_FAIL = "2401";
    //---------TEMPLATE-----------
    // TEMPLATE CREATE
    public final static String TEMPLATE_CREATE_SUCCESS = "3000";
    public final static String TEMPLATE_CREATE_EMPTY_ID = "3001";
    public final static String TEMPLATE_CREATE_EMPTY_NAME = "3002";
    public final static String TEMPLATE_CREATE_EMPTY_NODEID = "3003";
    public final static String TEMPLATE_CREATE_NODE_NOT_FOUND = "3004";
    public final static String TEMPLATE_CREATE_ALREADY_EXISTS = "3005";
    public final static String TEMPLATE_CREATE_TYPE_EMPTY = "3006";
    public final static String TEMPLATE_CREATE_ATTRS_EMPTY = "3007";
    public final static String TEMPLATE_CREATE_FAIL = "3008";
    // TEMPLATE DELETE
    public final static String TEMPLATE_DELETE_SUCCESS = "3100";
    public final static String TEMPLATE_DELETE_NOT_FOUND = "3101";
    public final static String TEMPLATE_DELETE_EMPTY_ID = "3102";
    public final static String TEMPLATE_DELETE_FAIL = "3103";
    // TEMPLATE MODIFY
    public final static String TEMPLATE_MODIFY_SUCCESS = "3200";
    public final static String TEMPLATE_MODIFY_EMPTY_ID = "3201";
    public final static String TEMPLATE_MODIFY_NOT_FOUND = "3202";
    public final static String TEMPLATE_MODIFY_FAIL = "3203";
    // TEMPLATE ADD ATTR
    public final static String TEMPLATE_ADD_ATTR_SUCCESS = "3300";
    public final static String TEMPLATE_ADD_ATTR_EMPTY_ID = "3301";
    public final static String TEMPLATE_ADD_ATTR_NOT_FOUND = "3302";
    public final static String TEMPLATE_ADD_ATTR_EMPTY_NAME = "3303";
    public final static String TEMPLATE_ADD_ATTR_EMPTY_INTRO = "3304";
    public final static String TEMPLATE_ADD_ATTR_FAIL = "3305";
    // TEMPLATE DEL ATTR
    public final static String TEMPLATE_DEL_ATTR_SUCCESS = "3400";
    public final static String TEMPLATE_DEL_ATTR_EMPTY_ID = "3401";
    public final static String TEMPLATE_DEL_ATTR_NOT_FOUND = "3402";
    public final static String TEMPLATE_DEL_ATTR_EMPTY_NAME = "3403";
    public final static String TEMPLATE_DEL_ATTR_FAIL = "3404";
    // TEMPLATE GET
    public final static String TEMPLATE_GET_SUCCESS = "3500";
    public final static String TEMPLATE_GET_FAIL = "3501";
    //-----------USER--------------
    // USER REGISTER
    public final static String USER_REGISTER_SUCCESS = "4000";
    public final static String USER_REGISTER_DUPLICATE_USERNAME = "4001";
    // USER UNREGISTER
    public final static String USER_UNREGISTER_SUCCESS = "4100";
    public final static String USER_UNREGISTER_FAIL = "4101";
    //----------SUBSCRIBE-----------
    // DELETE TEMPLATE SUBSCRIBE MESSAGE
    public final static String SUB_MSG_DEL_TEMPLATE_SUCCESS = "5000";
    public final static String SUB_MSG_DEL_TEMPLATE_NOT_FOUND = "5001";
    public final static String SUB_MSG_DEL_TEMPLATE_FAIL = "5002";
    // DELETE ENTITY SUBSCRIBE MESSAGE
    public final static String SUB_MSG_DEL_ENTITY_SUCCESS = "5100";
    public final static String SUB_MSG_DEL_ENTITY_NOT_FOUND = "5101";
    public final static String SUB_MSG_DEL_ENTITY_FAIL = "5102";
    public final static String SUB_MSG_DEL_TYPE_ERROR = "5103";
    // ADD ENTITY SUBSCRIBER
    public final static String SUB_ADD_ENTITY_SUCCESS = "5200";
    public final static String SUB_ADD_ENTITY_EMPTY_ID = "5201";
    public final static String SUB_ADD_ENTITY_EMPTY_USER_ID = "5202";
    public final static String SUB_ADD_ENTITY_USER_NOT_FOUND = "5203";
    public final static String SUB_ADD_ENTITY_NOT_FOUND = "5204";
    public final static String SUB_ADD_ENTITY_FAIL = "5205";
    // DEL ENTITY SUBSCRIBER
    public final static String SUB_DEL_ENTITY_SUCCESS = "5300";
    public final static String SUB_DEL_ENTITY_EMPTY_ID = "5301";
    public final static String SUB_DEL_ENTITY_EMPTY_USER_ID = "5302";
    public final static String SUB_DEL_ENTITY_USER_NOT_FOUND = "5303";
    public final static String SUB_DEL_ENTITY_FAIL = "5304";
    // ADD TEMPLATE SUBSCRIBER
    public final static String SUB_ADD_TEMPLATE_SUCCESS = "5400";
    public final static String SUB_ADD_TEMPLATE_EMPTY_ID = "5401";
    public final static String SUB_ADD_TEMPLATE_EMPTY_USER_ID = "5402";
    public final static String SUB_ADD_TEMPLATE_USER_NOT_FOUND = "5403";
    public final static String SUB_ADD_TEMPLATE_NOT_FOUND = "5404";
    public final static String SUB_ADD_TEMPLATE_FAIL = "5405";
    // DEL TEMPLATE SUBSCRIBER
    public final static String SUB_DEL_TEMPLATE_SUCCESS = "5500";
    public final static String SUB_DEL_TEMPLATE_EMPTY_ID = "5501";
    public final static String SUB_DEL_TEMPLATE_EMPTY_USER_ID = "5502";
    public final static String SUB_DEL_TEMPLATE_USER_NOT_FOUND = "5503";
    public final static String SUB_DEL_TEMPLATE_FAIL = "5504";

}
