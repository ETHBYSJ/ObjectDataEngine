package com.sjtu.objectdataengine.utils.Result;

public enum ResultCodeEnum {
    //-----------TREE--------------
    // TREE CREATE
    TREE_CREATE_SUCCESS("2000", "创建成功"),
    TREE_CREATE_EMPTY_ID("2001", "ID不能为空"),
    TREE_CREATE_EMPTY_NAME("2002", "name不能为空"),
    TREE_CREATE_EMPTY_PARENT("2003", "父节点不能为空"),
    TREE_CREATE_FAIL("2004", "创建失败"),
    // TREE DELETE
    TREE_DELETE_SUCCESS("2100", "删除成功"),
    TREE_DELETE_EMPTY_ID("2101", "ID不能为空"),
    TREE_DELETE_NODE_NOT_FOUND("2102", "节点不存在"),
    TREE_DELETE_FAIL("2103", "删除失败"),
    // TREE MODIFY
    TREE_MODIFY_SUCCESS("2200", "修改成功"),
    TREE_MODIFY_EMPTY_ID("2201", "ID不能为空"),
    TREE_MODIFY_NODE_NOT_FOUND("2202", "节点不存在"),
    TREE_MODIFY_FAIL("2203", "修改失败"),
    // GET TREE
    TREE_GET_SUCCESS("2300", "查询成功"),
    TREE_GET_FAIL("2301", "查询失败"),
    // GET NODE
    NODE_GET_SUCCESS("2400", "查询成功"),
    NODE_GET_FAIL("2401", "查询失败"),
    //---------TEMPLATE-----------
    // TEMPLATE CREATE
    TEMPLATE_CREATE_SUCCESS("3000", "创建成功"),
    TEMPLATE_CREATE_EMPTY_ID("3001", "ID不能为空"),
    TEMPLATE_CREATE_EMPTY_NAME("3002", "name不能为空"),
    TEMPLATE_CREATE_EMPTY_NODEID("3003", "nodeId不能为空"),
    TEMPLATE_CREATE_NODE_NOT_FOUND("3004", "节点不存在"),
    TEMPLATE_CREATE_ALREADY_EXISTS("3005", "指定节点上已经绑定模板，若想创建新模板请先删除旧模板"),
    TEMPLATE_CREATE_TYPE_EMPTY("3006", "类型不能为空"),
    TEMPLATE_CREATE_ATTRS_EMPTY("3007", "属性不能为空"),
    TEMPLATE_CREATE_FAIL("3008", "创建失败"),
    // TEMPLATE DELETE
    TEMPLATE_DELETE_SUCCESS("3100", "删除成功"),
    TEMPLATE_DELETE_NOT_FOUND("3101", "模板不存在"),
    TEMPLATE_DELETE_EMPTY_ID("3102", "ID不能为空"),
    TEMPLATE_DELETE_FAIL("3103", "删除失败"),
    // TEMPLATE MODIFY
    TEMPLATE_MODIFY_SUCCESS("3200", "修改成功"),
    TEMPLATE_MODIFY_EMPTY_ID("3201", "ID不能为空"),
    TEMPLATE_MODIFY_NOT_FOUND("3202", "模板不存在"),
    TEMPLATE_MODIFY_FAIL("3203", "修改失败"),
    // TEMPLATE ADD ATTR
    TEMPLATE_ADD_ATTR_SUCCESS("3300", "增加成功"),
    TEMPLATE_ADD_ATTR_EMPTY_ID("3301", "ID不能为空"),
    TEMPLATE_ADD_ATTR_NOT_FOUND("3302", "模板不存在"),
    TEMPLATE_ADD_ATTR_EMPTY_NAME("3303", "属性名不能为空"),
    TEMPLATE_ADD_ATTR_EMPTY_INTRO("3304", "nickname不能为空"),
    TEMPLATE_ADD_ATTR_FAIL("3305", "增加失败"),
    // TEMPLATE DEL ATTR
    TEMPLATE_DEL_ATTR_SUCCESS("3400", "删除成功"),
    TEMPLATE_DEL_ATTR_EMPTY_ID("3401", "ID不能为空"),
    TEMPLATE_DEL_ATTR_NOT_FOUND("3402", "模板不存在"),
    TEMPLATE_DEL_ATTR_EMPTY_NAME("3403", "属性名不能为空"),
    TEMPLATE_DEL_ATTR_FAIL("3404", "删除失败"),
    // TEMPLATE GET
    TEMPLATE_GET_SUCCESS("3500", "查询成功"),
    TEMPLATE_GET_FAIL("3501", "查询失败");

    private String code;
    private String msg;
    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
