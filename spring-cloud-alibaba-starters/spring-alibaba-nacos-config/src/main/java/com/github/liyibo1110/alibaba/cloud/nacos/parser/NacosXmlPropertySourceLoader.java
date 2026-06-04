package com.github.liyibo1110.alibaba.cloud.nacos.parser;

import com.github.liyibo1110.alibaba.cloud.nacos.utils.StringUtils;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载Nacos XML格式的Loader实现
 * @author liyibo
 * @date 2026-06-03 15:01
 */
public class NacosXmlPropertySourceLoader extends AbstractPropertySourceLoader implements Ordered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public String[] getFileExtensions() {
        return new String[] { "xml" };
    }

    @Override
    protected List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException {
        Map<String, Object> nacosDataMap = parseXml2Map(resource);
        return Collections.singletonList(new OriginTrackedMapPropertySource(name, nacosDataMap, true));
    }

    private Map<String, Object> parseXml2Map(Resource resource) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>(32);
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(resource.getInputStream());
            if (null == document)
                return null;
            parseNodeList(document.getChildNodes(), map, "");
        } catch (Exception e) {
            throw new IOException("The xml content parse error.", e.getCause());
        }
        return map;
    }

    private void parseNodeList(NodeList nodeList, Map<String, Object> map, String parentKey) {
        if (nodeList == null || nodeList.getLength() < 1)
            return;

        parentKey = parentKey == null ? "" : parentKey;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String value = node.getNodeValue();
            value = value == null ? "" : value.trim();
            String name = node.getNodeName();
            name = name == null ? "" : name.trim();

            if (StringUtils.isEmpty(name))
                continue;

            String key = StringUtils.isEmpty(parentKey) ? name : parentKey + AbstractPropertySourceLoader.DOT + name;
            NamedNodeMap nodeMap = node.getAttributes();
            parseNodeAttr(nodeMap, map, key);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
                parseNodeList(node.getChildNodes(), map, key);
                continue;
            }
            if (value.length() < 1)
                continue;

            map.put(parentKey, value);
        }
    }

    private void parseNodeAttr(NamedNodeMap nodeMap, Map<String, Object> map, String parentKey) {
        if (null == nodeMap || nodeMap.getLength() < 1)
            return;
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node node = nodeMap.item(i);
            if (null == node)
                continue;
            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                if (StringUtils.isEmpty(node.getNodeName()))
                    continue;
                if (StringUtils.isEmpty(node.getNodeValue()))
                    continue;
                map.put(String.join(AbstractPropertySourceLoader.DOT, parentKey, node.getNodeName()), node.getNodeValue());
            }
        }
    }
}
