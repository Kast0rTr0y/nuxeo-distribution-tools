/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu, jcarsique
 */
package org.nuxeo.build.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.tools.ant.BuildException;
import org.nuxeo.build.maven.filter.VersionManagement;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ArtifactDescriptor {

    public String groupId = null;

    public String artifactId = null;

    public String version = null;

    public String type = "jar";

    public String classifier = null;

    public String scope = "compile";

    public static ArtifactDescriptor emptyDescriptor() {
        ArtifactDescriptor ad = new ArtifactDescriptor();
        ad.scope = null;
        ad.type = null;
        return ad;
    }

    public ArtifactDescriptor() {
    }

    public ArtifactDescriptor(String groupId, String artifactId,
            String version, String type, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
    }

    public ArtifactDescriptor(String expr) {
        parse(expr);
    }

    public void parse(String expr) {
        String[] result = expr.split(":");
        if (result.length > 5) {
            scope = "".equals(result[5]) ? "compile" : result[5];
        }
        if (result.length > 4) {
            classifier = "".equals(result[4]) ? null : result[4];
        }
        if (result.length > 3) {
            type = "".equals(result[3]) ? "jar" : result[3];
        }
        if (result.length > 2) {
            version = "".equals(result[2]) ? null : result[2];
        }
        if (result.length > 1) {
            artifactId = "".equals(result[1]) ? null : result[1];
        }
        if (result.length > 0) {
            groupId = "".equals(result[0]) ? null : result[0];
        }
    }

    public Artifact toBuildArtifact() {
        return MavenClientFactory.getInstance().getArtifactFactory().createBuildArtifact(
                groupId, artifactId, version, type);
    }

    public Artifact toArtifactWithClassifier() {
        return MavenClientFactory.getInstance().getArtifactFactory().createArtifactWithClassifier(
                groupId, artifactId, version, type, classifier);
    }

    public String getNodeKeyPattern() {
        if (groupId != null) {
            StringBuilder buf = new StringBuilder();
            buf.append(groupId);
            if (artifactId != null) {
                buf.append(':').append(artifactId);
                if (version != null) {
                    buf.append(':').append(version);
                    if (type != null) {
                        buf.append(':').append(type);
                    }
                }
            }
            return buf.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(groupId).append(':').append(artifactId);
        buf.append(':').append(version);
        buf.append(':').append(type);
        buf.append(':').append(classifier);
        buf.append(':').append(scope);
        return buf.toString();
    }

    /**
     * @return ArtifactFactory().createDependencyArtifact()
     * @since 1.10.2
     */
    public Artifact getArtifact() {
        MavenClient maven = MavenClientFactory.getInstance();
        // Resolve version if not provided
        if (version == null) {
            VersionManagement versionManagement = maven.getGraph().getVersionManagement();
            version = versionManagement.getVersion(this);
            if (version == null) {
                throw new BuildException(
                        "Version is required since not found in dependency management: "
                                + this);
            }
        }
        Artifact artifact = maven.getArtifactFactory().createDependencyArtifact(
                groupId, artifactId, VersionRange.createFromVersion(version),
                type, classifier, scope);
        return artifact;
    }

    /**
     * Should be equivalent to {@link #getArtifact()}...
     *
     * @return ArtifactFactory().createBuildArtifact()
     * @since 1.10.2
     */
    public Artifact getBuildArtifact() {
        return MavenClientFactory.getInstance().getArtifactFactory().createBuildArtifact(
                groupId, artifactId, version, type);
    }

}
