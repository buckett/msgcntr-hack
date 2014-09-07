<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
        <script type="text/javascript" src="/library/js/jquery/jquery-1.9.1.min.js"></script>
        <sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
		<script type="text/javascript">
			$(document).ready(function(){
				//fade permission block and then disable all the inputs/selects in the permission include so as not to confuse people
				$('#permissionReadOnly').fadeTo("fast", 0.50);
				// cannot seem to disable these controls and still submit
				// $('#permissionReadOnly input, #permissionReadOnly select').attr('disabled', 'disabled');
				//toggle the long description, hiding the hide link, then toggling the hide, show links and description
				$('a#hide').hide();
				$('#toggle').hide();
				$('a#show,a#hide').click(function(){
					$('#toggle,a#hide,a#show').toggle();
					resizeFrame('grow');
					return false;
				});
			});
		</script>
		<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
		%>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>			
		<%--//designNote: this just feels weird - presenting somehting that sort of looks like the form used to create the topic (with an editable permissions block!) to comfirm deletion --%>
<!--jsp/discussionForum/topic/dfTopicSettings.jsp-->
		<%--<sakai:tool_bar_message value="#{msgs.cdfm_delete_topic_title}"/>--%>
        
		<h:outputText styleClass="messageAlert" style="display:block" value="#{msgs.cdfm_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"/>
        <h:outputText styleClass="messageAlert" value="#{msgs.cdfm_duplicate_topic_confirm}" rendered="#{ForumTool.selectedTopic.markForDuplication}" style="display:block" />
		<div class="topicBloc" style="padding:0 .5em"><h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
			<p>
				<span class="title">
					<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedTopic.topic.locked=='true'}"  style="margin-right:.3em"/>
					<h:graphicImage url="/images/silk/lock_open.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedTopic.topic.locked=='false'}"  style="margin-right:.3em"/>
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" rendered="#{!ForumTool.selectedTopic.markForDuplication}"/>
                    <h:inputText size="50" value="#{ForumTool.selectedTopic.topic.title}" id="topic_title" rendered="#{ForumTool.selectedTopic.markForDuplication}">
                        <f:validateLength maximum="255" minimum="1" />
                    </h:inputText>                   
                    
				</span>
				<h:outputText   value="#{msgs.cdfm_openb}"/>
				<h:outputText   value="#{msgs.cdfm_moderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='true'}" />
				<h:outputText   value="#{msgs.cdfm_notmoderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='false'}" />
				<h:outputText   value="#{msgs.cdfm_closeb}"/>

				</p>
			<p class="textPanel">
				    <h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
			</p>
			<p><a id="show" class="show"  href="#">
				<h:graphicImage url="/images/collapse.gif" alt="" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
			</a></p>
			<p><a id="hide" class="hide"  href="#">
				<h:graphicImage url="/images/expand.gif" alt="" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
			</a></p>
				
			<div class="textPanel toggle"  id="toggle">
				<mf:htmlShowArea hideBorder="true" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
				<h:dataTable value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" styleClass="listHier" columnClasses="attach,bogus">
				  <h:column>
					<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />					
					</h:column>
					  <h:column>
					<h:outputLink value="#{eachAttach.url}" target="_blank">
					  <h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
				    </h:outputLink>
				  </h:column>
				</h:dataTable>
			</div>
		</div>
       <div class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTopicSettings}" id="revise"  
                           value="#{msgs.cdfm_button_bar_revise}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           accesskey="r" styleClass="active"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>        
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton id="duplicate" action="#{ForumTool.processActionDuplicateTopic}" 
                           value="#{msgs.cdfm_duplicate_topic}" rendered="#{ForumTool.selectedTopic.markForDuplication}"
                           accesskey="s" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton immediate="true" action="#{ForumTool.processReturnToOriginatingPage}" id="cancel" 
                           value="#{msgs.cdfm_button_bar_cancel} " accesskey="x" />
         <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
