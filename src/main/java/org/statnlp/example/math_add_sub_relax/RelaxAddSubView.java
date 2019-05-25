package org.statnlp.example.math_add_sub_relax;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import org.statnlp.commons.types.Label;
import org.statnlp.commons.types.Sentence;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.example.math_add_sub_relax.RelaxAddSubNetworkCompiler.NodeType;
import org.statnlp.example.math_add_sub_relax.RelaxAddSubNetworkCompiler.SubNodeType;
import org.statnlp.hypergraph.NetworkIDMapper;
import org.statnlp.ui.visualize.type.VNode;
import org.statnlp.ui.visualize.type.VisualizationViewerEngine;
import org.statnlp.ui.visualize.type.VisualizeGraph;
import org.statnlp.util.instance_parser.InstanceParser;

public class RelaxAddSubView extends VisualizationViewerEngine {

	static double span_width = 100;

	static double span_height = 100;

	static double offset_width = 100;

	static double offset_height = 100;

	/**
	 * The list of instances to be visualized
	 */
	protected LatentAddSubInstance instance;

	protected Sentence inputs;

	protected ArrayList<String> outputs;

	/**
	 * The list of labels to be used in the visualization.<br>
	 * This member is used to support visualization on those codes that do not
	 * utilize the generic pipeline. In the pipeline, use {@link #instanceParser}
	 * instead.
	 */
	protected Map<Integer, Label> labels;

	public RelaxAddSubView(Map<Integer, Label> labels) {
		super(null);
		this.labels = labels;
	}

	public RelaxAddSubView(InstanceParser instanceParser) {
		super(instanceParser);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initData() {
		this.instance = (LatentAddSubInstance) super.instance;
		this.inputs = (Sentence) super.inputs;
		this.outputs = (ArrayList<String>) super.outputs;
		// WIDTH = instance.Length * span_width;
	}

	@Override
	protected String label_mapping(VNode node) {
		int[] ids = node.ids;
		long node_id = NetworkIDMapper.toHybridNodeID(ids);
		// int size = instance.size();
		int pos = ids[1]; // position
		int signId = ids[2];
		String sign = signId == 0 ? "0" : signId == 1 ? "-" : signId == 2 ? "+" : "error";
		int nodeType = ids[3];
		int subNodeType = ids[4];

		if (nodeType == NodeType.leaf.ordinal()) {
			return "LEAF" + node_id;
		} else if (nodeType == NodeType.root.ordinal()) {
			return "ROOT" + " " + node_id;
		} else if (subNodeType == SubNodeType.before.ordinal()) {
			return this.inputs.get(pos).getForm() + " B " + sign + " " + node_id;
		} else if (subNodeType == SubNodeType.number.ordinal()) {
			return this.inputs.get(pos).getForm() + " N " + sign + " " + node_id;
		} else if (subNodeType == SubNodeType.after.ordinal()) {
			return this.inputs.get(pos).getForm() + " A " + sign + " " + node_id;
		} else {
			return "others";
		}

	}

	@Override
	protected void initNodeColor(VisualizeGraph vg) {
		if (colorMap != null) {
			for (VNode node : vg.getNodes()) {
				int[] ids = node.ids;
				int pos = ids[1];
				int signId = ids[2];
				int nodeType = ids[3];
				int subNodeType = ids[4];

				if (nodeType != NodeType.num.ordinal()) {
					node.color = colorMap[0];
				} else {
					if (subNodeType == SubNodeType.before.ordinal()) {
						node.color = colorMap[1 + (signId % 4)];
					} else if (subNodeType == SubNodeType.number.ordinal()) {
						node.color = colorMap[1 + (signId % 8)];
					} else if (subNodeType == SubNodeType.after.ordinal()) {
						node.color = colorMap[1 + (signId % 16)];
					}
				}
			}
		}

	}

	@Override
	protected void initNodeCoordinate(VisualizeGraph vg) {
		for (VNode node : vg.getNodes()) {
			int[] ids = node.ids;
			// int size = this.inputs.size();
			int pos = ids[1];
			int labelId = ids[2];
			int nodeType = ids[3];
			int subNodeType = ids[4];
			double x = pos * span_width;
			int mappedId = nodeType;
			switch (mappedId) {
			case 0:
				mappedId = 2;
				break;
			case 2:
				mappedId = 2;
				break;
			case 1:
				mappedId = 2;
				break;
			}
			double y = mappedId * span_height + offset_height;
			double signMove = 0.0;
			if (nodeType == NodeType.num.ordinal()) {

				if (subNodeType == SubNodeType.number.ordinal()) {
					signMove = 0.0;
				} else if (subNodeType == SubNodeType.before.ordinal()) {
					signMove = -1;
				} else if (subNodeType == SubNodeType.after.ordinal()) {
					signMove = 1;
				}
				if (labelId == 0) {
					x = (pos + 1) * span_width;
					y = (0 + signMove) * span_height + offset_height;
				} else if (labelId == 1) {
					x = (pos + 1) * span_width;
					y = (3 + signMove) * span_height + offset_height;
				} else if (labelId == 2) {
					x = (pos + 1) * span_width;
					y = (6 + signMove) * span_height + offset_height;
				}

			}

			node.point = new Point2D.Double(x, y);
			layout.setLocation(node, node.point);
			layout.lock(node, true);
		}
	}

}
