package com.zhukew.circle.server.util;

import com.zhukew.circle.api.common.TreeNode;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 树工具类
 *
 * @author: Wei
 */
public class TreeUtils {

    /**
     * 封装树结构，返回根节点
     */
    public static <T extends TreeNode> List<T> buildTree(List<T> nodes) {

        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        // 父节点相同的孩子分组到一起
        Map<Long, List<TreeNode>> groups = nodes.stream().collect(Collectors.groupingBy(TreeNode::getNodePId));
        return nodes.stream().filter(Objects::nonNull).peek(pnd -> {
            // 封装该节点的所有孩子
            List<TreeNode> ts = groups.get(pnd.getNodeId());
            pnd.setChildren(ts);
        }).filter(TreeNode::getRootNode).collect(Collectors.toList());

    }

    /**
     * 找到目标节点及所有孩子
     */
    public static <T extends TreeNode> void findAll(List<T> result, TreeNode node, Long targetId) {

        if (node.getNodeId().equals(targetId) || node.getNodePId().equals(targetId)) {
            addAll(result, node);
        } else {
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                for (TreeNode child : node.getChildren()) {
                    findAll(result, child, targetId);
                }
            }
        }

    }


    private static <T extends TreeNode> void addAll(List<T> result, TreeNode node) {
        result.add((T) node);
        if (!CollectionUtils.isEmpty(node.getChildren())) {
            for (TreeNode child : node.getChildren()) {
                addAll(result, child);
            }
        }
    }

}
