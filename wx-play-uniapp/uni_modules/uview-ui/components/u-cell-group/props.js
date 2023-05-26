// yangbuyi Copyright (c) https://yby6.com 2023.

export default {
    props: {
        // 分组标题
        title: {
            type: String,
            default: uni.$u.props.cellGroup.title
        },
        // 是否显示外边框
        border: {
            type: Boolean,
            default: uni.$u.props.cellGroup.border
        }
    }
}
