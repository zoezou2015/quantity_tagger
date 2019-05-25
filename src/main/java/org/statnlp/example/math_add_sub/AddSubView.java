package org.statnlp.example.math_add_sub;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import org.statnlp.commons.types.Label;
import org.statnlp.example.math_add_sub.AddSubNetworkCompiler.NodeType;
import org.statnlp.hypergraph.NetworkIDMapper;
import org.statnlp.ui.visualize.type.VNode;
import org.statnlp.ui.visualize.type.VisualizationViewerEngine;
import org.statnlp.ui.visualize.type.VisualizeGraph;
import org.statnlp.util.instance_parser.InstanceParser;

public class AddSubView extends VisualizationViewerEngine {

	static double span_width = 100;

	static double span_height = 100;

	static double offset_width = 100;

	static double offset_height = 100;

	/**
	 * The list of instances to be visualized
	 */
	protected AddSubInstance instance;

	protected ArrayList<String> inputs;

	protected ArrayList<String> outputs;

	/**
	 * The list of labels to be used in the visualization.<br>
	 * This member is used to support visualization on those codes that do not
	 * utilize the generic pipeline. In the pipeline, use {@link #instanceParser}
	 * instead.
	 */
	protected Map<Integer, Label> labels;

	public AddSubView(Map<Integer, Label> labels) {
		super(null);
		this.labels = labels;
	}

	public AddSubView(InstanceParser instanceParser) {
		super(instanceParser);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initData() {
		this.instance = (AddSubInstance) super.instance;
		this.inputs = (ArrayList<String>) super.inputs;
		this.outputs = (ArrayList<String>) super.outputs;
		// WIDTH = instance.Length * span_width;
	}

	@Override
	protected String label_mapping(VNode node) {
		int[] ids = node.ids;
		long node_id = NetworkIDMapper.toHybridNodeID(ids);
		// int size = instance.size();
		int pos = ids[0]; // position
		int signId = ids[1];
		String sign = signId == 0 ? "0" : signId == 1 ? "-" : signId == 2 ? "+" : "error";
		int nodeType = ids[2];

		if (nodeType == NodeType.leaf.ordinal()) {
			return "LEAF " + node_id;
		} else if (nodeType == NodeType.root.ordinal()) {
			return "ROOT" + " " + node_id;
		} else if (nodeType == NodeType.number.ordinal()) {
			return this.inputs.get(pos) + " " + sign + " " + node_id;
		} else {
			return "others";
		}

	}

	@Override
	protected void initNodeColor(VisualizeGraph vg) {
		if (colorMap != null) {
			for (VNode node : vg.getNodes()) {
				int[] ids = node.ids;
				int pos = ids[0];
				int signId = ids[1];
				int nodeType = ids[2];

				if (nodeType != NodeType.number.ordinal()) {
					node.color = colorMap[0];
				} else {
					node.color = colorMap[1 + (signId % 8)];
				}
			}
		}

	}

	@Override
	protected void initNodeCoordinate(VisualizeGraph vg) {
		for (VNode node : vg.getNodes()) {
			int[] ids = node.ids;
			// int size = this.inputs.size();
			int pos = ids[0];
			int labelId = ids[1];
			int nodeType = ids[2];
			double x = pos * span_width;
			int mappedId = nodeType;
			switch (mappedId) {
			case 0:
				mappedId = 2;
				break;
			case 2:
				mappedId = 6;
				break;
			case 1:
				mappedId = 3;
				break;
			}
			double y = mappedId * span_height + offset_height;
			double signMove = 0.0;
			if (nodeType == NodeType.number.ordinal()) {

				if (labelId == 0) {
					x = (pos) * span_width;
					y = (0 + signMove) * span_height + offset_height;
				} else if (labelId == 1) {
					x = (pos) * span_width;
					y = (3 + signMove) * span_height + offset_height;
				} else if (labelId == 2) {
					x = (pos) * span_width;
					y = (6 + signMove) * span_height + offset_height;
				}

			}

			node.point = new Point2D.Double(x, y);
			layout.setLocation(node, node.point);
			layout.lock(node, true);
		}
	}

}
