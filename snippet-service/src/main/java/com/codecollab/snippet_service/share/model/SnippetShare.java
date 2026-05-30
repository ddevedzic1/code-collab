package com.codecollab.snippet_service.share.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import com.codecollab.snippet_service.snippet.model.Snippet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "snippet_shares")
@SQLRestriction("end_date IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class SnippetShare {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "snippet_id", nullable = false)
	private Snippet snippet;

	@Column(name = "share_token", nullable = false, length = 300)
	private String shareToken;

	@Enumerated(EnumType.STRING)
	@Column(name = "share_type", nullable = false, length = 30)
	private ShareType shareType;

	@Enumerated(EnumType.STRING)
	@Column(name = "permission", nullable = false, length = 30)
	private Permission permission;

	@Column(name = "start_date", nullable = false, updatable = false, insertable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
	private LocalDateTime createdAt;

	@Column(name = "created_by", length = 300)
	private String createdBy;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", length = 300)
	private String updatedBy;
}
