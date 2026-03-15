// Auth

export interface UserSummary {
  id: string
  email: string
  displayName: string
  avatarUrl?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserSummary
}

// Workspace

export interface Workspace {
  id: string
  name: string
  slug: string
  description?: string
  logoUrl?: string
  isActive: boolean
  createdAt: string
  memberCount: number
  teamCount: number
}

// Team 

export interface Team {
  id: string
  workspaceId: string
  name: string
  identifier: string
  description?: string
  color?: string
  icon?: string
  isActive: boolean
  createdAt: string
  memberCount: number
}

// Issue

export type IssueStatus = 'BACKLOG' | 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'CANCELLED'
export type IssuePriority = 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NONE'

export interface Label {
  id: string
  name: string
  color: string
  description?: string
}

export interface Issue {
  id: string
  identifier: string
  sequenceNumber: number
  title: string
  description?: string
  status: IssueStatus
  priority: IssuePriority
  assignee?: UserSummary
  createdBy: UserSummary
  teamId: string
  teamIdentifier: string
  projectId?: string
  projectName?: string
  cycleId?: string
  cycleName?: string
  parentId?: string
  estimate?: number
  dueDate?: string
  completedAt?: string
  sortOrder: number
  createdAt: string
  updatedAt: string
  labels: Label[]
  subIssueCount: number
  commentCount: number
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

// Project

export type ProjectStatus = 'BACKLOG' | 'PLANNED' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'

export interface Project {
  id: string
  teamId: string
  name: string
  description?: string
  status: ProjectStatus
  color?: string
  icon?: string
  startDate?: string
  targetDate?: string
  lead?: UserSummary
  createdBy: UserSummary
  createdAt: string
  issueCount: number
}

// Cycle

export type CycleStatus = 'DRAFT' | 'STARTED' | 'COMPLETED'

export interface Cycle {
  id: string
  teamId: string
  name: string
  description?: string
  status: CycleStatus
  startDate: string
  endDate: string
  createdBy: UserSummary
  createdAt: string
  issueCount: number
  completedIssueCount: number
}

// API Error

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors?: Record<string, string>
}
